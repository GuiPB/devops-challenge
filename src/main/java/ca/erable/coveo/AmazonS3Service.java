package ca.erable.coveo;

import java.util.List;

import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.S3ObjectSummary;

public interface AmazonS3Service {
	List<Bucket> listBuckets();

	List<S3ObjectSummary> listObject(String bucketName);
}
