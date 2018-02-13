package ca.erable.devops;

import java.util.List;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.model.Bucket;

public interface AmazonS3Service {
    List<Bucket> listBuckets();

    BucketReport reportOnBucket(String bucketName);

    Regions getBucketLocation(String bucketName);
}
