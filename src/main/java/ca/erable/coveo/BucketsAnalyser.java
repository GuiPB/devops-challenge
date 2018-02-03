package ca.erable.coveo;

import java.util.ArrayList;
import java.util.List;

import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.S3ObjectSummary;

public class BucketsAnalyser {

	private AmazonS3Service awsS3;

	private List<BucketReport> reports = new ArrayList<>();
	private List<Bucket> bucketList = new ArrayList<>();

	public BucketsAnalyser(AmazonS3Service service) {
		awsS3 = service;
	}

	public boolean containsBucket(String first) {
		return bucketList.stream().anyMatch(e -> first.equals(e.getName()));
	}

	public BucketReport report(String bucketName) {
		return reports.stream().filter(e -> bucketName.equals(e.getName())).findFirst().orElse(null);
	}

	public void analyse() {
		bucketList = awsS3.listBuckets();
		bucketList.stream().forEach(t -> {
			List<S3ObjectSummary> objects = awsS3.listObject(t.getName());
			reports.add(new BucketReport(t.getName(), t.getCreationDate(), objects));
		});
	}
}
