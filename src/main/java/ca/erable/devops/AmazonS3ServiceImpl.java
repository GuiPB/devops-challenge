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

public class AmazonS3ServiceImpl implements AmazonS3Service {

    private static Logger log = LogManager.getLogger(AmazonS3ServiceImpl.class);
    private AmazonS3 defaultClient;
    private Map<String, Regions> locationByBucket = new HashMap<>();
    private Map<String, AmazonS3> clientsByBucket = new HashMap<>();
    private AmazonS3ClientBuilder clientBuilder;

    public AmazonS3ServiceImpl(AmazonS3ClientBuilder clientBuilderParam) {
        defaultClient = clientBuilderParam.build();
        clientBuilder = clientBuilderParam;
    }

    @Override
    public List<Bucket> listBuckets() {
        List<Bucket> listBuckets = defaultClient.listBuckets();

        // Pour chaque bucket, constuire un client avec la bonne region.
        log.debug(() -> "Creating a client for each bucket");
        listBuckets.stream().forEach(bucket -> {
            String bucketLocation = defaultClient.getBucketLocation(bucket.getName());
            Regions bucketRegion = Regions.DEFAULT_REGION;
            log.debug(() -> "Region " + bucketLocation + " for bucket " + bucket.getName());
            locationByBucket.put(bucket.getName(), bucketRegion);
            clientsByBucket.put(bucket.getName(), AmazonS3ClientBuilder.standard().withRegion(bucketRegion).build());
        });

        return listBuckets;
    }

    @Override
    public BucketReport reportOnBucket(String bucketName, StorageFilter byStorage) throws InterruptedException {
        log.debug(() -> "Report on bucket " + bucketName);
        AmazonS3 clientForBucket = clientsByBucket.get(bucketName);

        log.debug(() -> "Listing bucket root with delimiter '/'");
        ObjectListing rootListObjects = clientForBucket.listObjects(new ListObjectsRequest(bucketName, null, null, "/", null));

        List<String> commonPrefixes = rootListObjects.getCommonPrefixes();
        log.debug(() -> "Common prefixes: " + String.join(",", commonPrefixes));

        List<DirectoryWorker> workers = new ArrayList<>();
        List<DirectoryResult> collectedStats = new ArrayList<>();

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
            executor.awaitTermination(5, TimeUnit.HOURS);

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

        Integer numberOfFile = collectedStats.parallelStream().mapToInt(DirectoryResult::getFileCount).sum();
        numberOfFile += objects.size();

        return new BucketReport(bucketName, new Date(), Regions.AP_NORTHEAST_1, numberOfFile, totalSize, new Date());
    }

    @Override
    public BucketReport reportOnBucket(String string) throws InterruptedException {
        return reportOnBucket(string, StorageFilter.NO_FILTER);
    }

    @Override
    public Regions getBucketLocation(String bucketName) {
        return locationByBucket.getOrDefault(bucketName, Regions.DEFAULT_REGION);
    }

}
