package ca.erable.coveo;

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
	private Map<String, AmazonS3> clientsByBucket = new HashMap<>();

	public AmazonS3ServiceImpl(Regions region) {
		defaultClient = AmazonS3ClientBuilder.standard().withRegion(region).build();
	}

	@Override
	public List<Bucket> listBuckets() {
		List<Bucket> listBuckets = defaultClient.listBuckets();
		// Pour chaque bucket, constuire un client avec la bonne region.
		listBuckets.stream().forEach((e -> {
			String bucketLocation = defaultClient.getBucketLocation(e.getName());
			clientsByBucket.put(e.getName(),
					AmazonS3ClientBuilder.standard().withRegion(Regions.fromName(bucketLocation)).build());
		}));

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

}
