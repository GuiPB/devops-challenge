package ca.erable.coveo;

import java.util.ArrayList;
import java.util.List;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;

public class AmazonS3ServiceImpl implements AmazonS3Service {

	private AmazonS3 defaultClient;

	public AmazonS3ServiceImpl(Regions region) {
		defaultClient = AmazonS3ClientBuilder.standard().withRegion(region).build();
	}

	@Override
	public List<Bucket> listBuckets() {
		return defaultClient.listBuckets();
	}

	@Override
	public List<S3ObjectSummary> listObject(String bucketName) {
		ObjectListing listObjects = defaultClient.listObjects(bucketName);

		List<S3ObjectSummary> objects = new ArrayList<>();

		objects.addAll(listObjects.getObjectSummaries());

		while (listObjects.isTruncated()) {
			ObjectListing listNextBatchOfObjects = defaultClient.listNextBatchOfObjects(listObjects);
			objects.addAll(listNextBatchOfObjects.getObjectSummaries());
		}

		return objects;
	}

}
