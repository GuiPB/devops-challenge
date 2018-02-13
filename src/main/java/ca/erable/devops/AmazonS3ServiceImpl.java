package ca.erable.devops;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.Bucket;
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
    public List<S3ObjectSummary> listObject(String bucketName) {
        AmazonS3 clientForBucket = clientsByBucket.get(bucketName);
        ObjectListing listObjects = clientForBucket.listObjects(bucketName);

        List<S3ObjectSummary> objects = new ArrayList<>();

        objects.addAll(listObjects.getObjectSummaries());

        while (listObjects.isTruncated()) {
            ObjectListing listNextBatchOfObjects = clientForBucket.listNextBatchOfObjects(listObjects);
            objects.addAll(listNextBatchOfObjects.getObjectSummaries());
        }

        return objects;
    }

    @Override
    public Regions getBucketLocation(String bucketName) {
        return locationByBucket.getOrDefault(bucketName, Regions.DEFAULT_REGION);
    }

}
