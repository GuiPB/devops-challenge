package ca.erable.coveo;

import java.util.List;

import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;

public interface AmazoneS3Service {
	List<Bucket> listBuckets();

	List<S3ObjectSummary> listObject(String bucketName);

	ObjectListing listNextBatchOfObjects(ObjectListing previousListing);

	String getBucketLocation(String bucketName);
}
