package ca.erable.devops;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;

/**
 * This class is an implementation of a {@link AmazonS3Service}. Before anything
 * else, listBucket() must be called first, for it creates a client for each
 * bucket location. Then after, any of the reportOnBucket() methods can be
 * invoked.
 * 
 * @author guillaume
 *
 */
public class AmazonS3ServiceImpl implements AmazonS3Service {

    private static Logger log = LogManager.getLogger(AmazonS3ServiceImpl.class);
    private AmazonS3 defaultClient;
    private Map<String, String> locationByBucket = new HashMap<>();
    private Map<String, AmazonS3> clientsByBucket = new HashMap<>();
    private AmazonS3ClientBuilder clientBuilder;
    private Map<String, Bucket> buckets = new HashMap<>();

    public AmazonS3ServiceImpl(AmazonS3ClientBuilder clientBuilderParam) {
        defaultClient = clientBuilderParam.build();
        clientBuilder = clientBuilderParam;
    }

    /**
     * This method does indeed list each bucket owned by a given ID and secret key.
     * It also creates a new client for each distinct bucket locations in cases
     * where multiple bucket are distributed in many regions. Only one client will
     * be effectively created for each bucket
     */
    @Override
    public List<Bucket> listBuckets() {
        List<Bucket> listBuckets = defaultClient.listBuckets();

        log.debug(() -> "Creating a client for each bucket location");
        listBuckets.stream().forEach(bucket -> {
            String bucketLocation = defaultClient.getBucketLocation(bucket.getName());
            log.debug(() -> "Region " + bucketLocation + " for bucket " + bucket.getName());
            locationByBucket.put(bucket.getName(), bucketLocation);
            clientsByBucket.put(bucket.getName(), clientBuilder.withRegion(bucketLocation).build());
            buckets.put(bucket.getName(), bucket);
        });

        return listBuckets;
    }

    /**
     * <p>
     * This method reports stats on a given bucket.
     * </p>
     * <p>
     * It does so by listing a first batch of key entries from the bucket. Then, it
     * creates a thread for each common prefixes it receives. Each thread will also
     * gather other common prefixes to process. This process will loop until there
     * is no sub-directory to analyse. Finally, the root directory is also analysed
     * and everything is summerized in {@link BucketReport}.
     * </p>
     * <p>
     * This process is intended to profit from prefixes as they can used to split
     * the workload on many workers. Since amazon is able to scale as requests grow
     * in number, the theory is that eache worker will be more efficient at
     * processing paginated results.
     * </p>
     */
    @Override
    public BucketReport reportOnBucket(String bucketName, StorageFilter byStorage) {
        log.debug(() -> "Report on bucket " + bucketName);
        AmazonS3 clientForBucket = clientsByBucket.get(bucketName);

        log.debug(() -> "Listing bucket root with delimiter '/'");
        ObjectListing rootListObjects = clientForBucket.listObjects(new ListObjectsRequest(bucketName, null, null, "/", null));

        List<String> commonPrefixes = rootListObjects.getCommonPrefixes();
        log.debug(() -> "Common prefixes: " + String.join(",", commonPrefixes));

        log.debug(() -> "Drilling down directories if applicable");
        List<DirectoryResult> collectedStats = drillDownDirectoryHierachy(bucketName, byStorage, clientForBucket, commonPrefixes);

        List<S3ObjectSummary> objects = new ArrayList<>();
        objects.addAll(rootListObjects.getObjectSummaries().stream().filter(y -> byStorage.isFiltred(y)).collect(toList()));

        while (rootListObjects.isTruncated()) {
            log.debug(() -> "Truncation detected. Listing bucket on root directory");
            rootListObjects = clientForBucket.listNextBatchOfObjects(rootListObjects);
            objects.addAll(rootListObjects.getObjectSummaries().stream().filter(y -> byStorage.isFiltred(y)).collect(toList()));
        }

        log.debug(() -> "Summerizing data");
        Long totalSize = collectedStats.parallelStream().mapToLong(DirectoryResult::getFileSize).sum();
        totalSize += objects.parallelStream().mapToLong(i -> i.getSize()).sum();

        Date lastModifiedRootFolder = objects.stream().map(S3ObjectSummary::getLastModified).filter(d -> d != null).sorted((a, b) -> b.compareTo(a)).findFirst().orElse(null);
        Date lastModifiedChildFolder = collectedStats.parallelStream().map(DirectoryResult::getLastModified).filter(d -> d != null).sorted((a, b) -> b.compareTo(a)).findFirst().orElse(null);

        Date lastModified = DateOrderUtils.returnLatest(lastModifiedRootFolder, lastModifiedChildFolder);

        Integer numberOfFile = collectedStats.parallelStream().mapToInt(DirectoryResult::getFileCount).sum();
        numberOfFile += objects.size();

        return new BucketReport(bucketName, buckets.get(bucketName).getCreationDate(), locationByBucket.get(bucketName), numberOfFile, totalSize, lastModified);
    }

    private List<DirectoryResult> drillDownDirectoryHierachy(String bucketName, StorageFilter byStorage, AmazonS3 clientForBucket, List<String> commonPrefixesParam) {
        List<DirectoryWorker> workers = new ArrayList<>();
        List<DirectoryResult> collectedStats = new ArrayList<>();
        List<String> commonPrefixes = commonPrefixesParam;

        while (!commonPrefixes.isEmpty()) {

            ExecutorService executor = Executors.newFixedThreadPool(10);
            List<DirectoryResult> newResults = new ArrayList<>();

            for (String prefix : commonPrefixes) {
                workers.add(new DirectoryWorker(prefix, clientForBucket, bucketName, byStorage));
                log.debug(() -> "Worker added on prefix: " + prefix + " and bucket " + bucketName);
            }
            try {
                newResults = executor.invokeAll(workers).stream().map(wk -> {
                    try {
                        return wk.get();
                    } catch (InterruptedException | ExecutionException e) {
                        throw new IllegalStateException();
                    }
                }).collect(toList());

            } catch (InterruptedException interup) {
                log.error(() -> interup.getMessage());
                executor.shutdownNow();
                throw new IllegalStateException();
            }

            executor.shutdown();
            log.debug(() -> "Awaiting termination for all workers");
            try {
                executor.awaitTermination(5, TimeUnit.HOURS);
            } catch (InterruptedException e) {
                log.error("Error while awaiting termination", e);
            }

            log.debug(() -> "Clearing current prefixes. Replace by new ones from each directory");
            commonPrefixes.clear();
            List<List<String>> collectedNewPrefixes = newResults.stream().map(t -> t.getCommonPrefixes()).filter(l -> !l.isEmpty()).collect(toList());

            for (List<String> prefList : collectedNewPrefixes) {
                commonPrefixes.addAll(prefList);
            }

            log.debug(() -> "New prefixes to process: " + String.join(",", commonPrefixes));
            collectedStats.addAll(newResults);
            log.debug("Clearing workers");
            workers.clear();
        }

        return collectedStats;
    }

    /**
     * Reports on given bucket without any filter.
     */
    @Override
    public BucketReport reportOnBucket(String name) {
        return reportOnBucket(name, StorageFilter.NO_FILTER);
    }

    @Override
    public String getBucketLocation(String bucketName) {
        return locationByBucket.getOrDefault(bucketName, Regions.DEFAULT_REGION.toString());
    }

}
