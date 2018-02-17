package ca.erable.devops;

import java.util.List;

import com.amazonaws.services.s3.model.Bucket;

public interface AmazonS3Service {
    List<Bucket> listBuckets();

    String getBucketLocation(String bucketName);

    BucketReport reportOnBucket(String name, StorageFilter byStorage);

    BucketReport reportOnBucket(String string);
}
