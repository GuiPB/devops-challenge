package ca.erable.devops;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;

public class AmazonS3ServiceImpl implements AmazonS3Service {

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
        listBuckets.stream().forEach(bucket -> {
            String bucketLocation = defaultClient.getBucketLocation(bucket.getName());
            Regions bucketRegion = Regions.fromName(bucketLocation);
            locationByBucket.put(bucket.getName(), bucketRegion);
            clientsByBucket.put(bucket.getName(), clientBuilder.withRegion(bucketRegion).build());
        });

        return listBuckets;
    }

    @Override
    public BucketReport reportOnBucket(String bucketName, StorageFilter byStorage) {
        AmazonS3 clientForBucket = clientsByBucket.get(bucketName);

        ObjectListing listObjects = clientForBucket.listObjects(new ListObjectsRequest(bucketName, null, null, "/", null));

        List<String> commonPrefixes = listObjects.getCommonPrefixes();

        ExecutorService executor = Executors.newCachedThreadPool();
        List<DirectoryWorker> workers = new ArrayList<>();

        for (String prefix : commonPrefixes) {
            workers.add(new DirectoryWorker(prefix, clientForBucket, bucketName, byStorage));
        }

        List<DirectoryResult> collectedStats = new ArrayList<>();

        try {
            collectedStats = executor.invokeAll(workers).stream().map(wk -> {
                try {
                    return wk.get();
                } catch (InterruptedException | ExecutionException e) {
                    throw new IllegalStateException();
                }
            }).collect(Collectors.toList());

        } catch (InterruptedException e1) {
            executor.shutdownNow();
            throw new IllegalStateException();
        }

        List<S3ObjectSummary> objects = new ArrayList<>();

        objects.addAll(listObjects.getObjectSummaries());

        while (listObjects.isTruncated()) {
            ObjectListing listNextBatchOfObjects = clientForBucket.listNextBatchOfObjects(listObjects);
            objects.addAll(listNextBatchOfObjects.getObjectSummaries());
        }

        Long totalSize = collectedStats.parallelStream().mapToLong(DirectoryResult::getFileSize).sum();
        totalSize += objects.parallelStream().mapToLong(i -> i.getSize()).sum();

        return new BucketReport(bucketName, new Date(), Regions.AP_NORTHEAST_1, 0, totalSize, new Date());
    }

    @Override
    public BucketReport reportOnBucket(String string) {
        return reportOnBucket(string, StorageFilter.NO_FILTER);
    }

    @Override
    public Regions getBucketLocation(String bucketName) {
        return locationByBucket.getOrDefault(bucketName, Regions.DEFAULT_REGION);
    }

}
