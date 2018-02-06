package ca.erable.devops;

import java.util.List;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.S3ObjectSummary;

public interface AmazonS3Service {
	List<Bucket> listBuckets();

	List<S3ObjectSummary> listObject(String bucketName);

	Regions getBucketLocation(String bucketName);
}
