package ca.erable.devops;

import java.util.List;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.model.Bucket;

public interface AmazonS3Service {
    List<Bucket> listBuckets();

    Regions getBucketLocation(String bucketName);

    BucketReport reportOnBucket(String name, StorageFilter byStorage) throws InterruptedException;

    BucketReport reportOnBucket(String string) throws InterruptedException;
}
