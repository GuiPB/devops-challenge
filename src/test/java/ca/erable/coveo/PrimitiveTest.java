package ca.erable.coveo;

import java.util.List;

import org.junit.Test;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;

public class PrimitiveTest {

	@Test
	public void test() {
		AmazonS3 defaultClient = AmazonS3ClientBuilder.standard().withRegion(Regions.US_EAST_2).build();

		List<Bucket> listBuckets = defaultClient.listBuckets();

		System.out.println("Buckets");
		listBuckets.stream().forEach((bk) -> {
			System.out.println(bk.getName());
			System.out.println(bk.getCreationDate());
			System.out.println(defaultClient.getBucketLocation(bk.getName()));
		});

		Long totalFileSize = new Long(0);
		Integer fileCount = new Integer(0);

		ListObjectsRequest req = new ListObjectsRequest("ca.erable.boisclair", null, null, "/", null);
		ObjectListing listObjects = defaultClient.listObjects(req);

		fileCount += listObjects.getObjectSummaries().size();
		totalFileSize += listObjects.getObjectSummaries().stream().mapToLong(S3ObjectSummary::getSize).sum();

		while (listObjects.isTruncated()) {
			listObjects = defaultClient.listNextBatchOfObjects(listObjects);
			fileCount += listObjects.getObjectSummaries().size();
			totalFileSize += listObjects.getObjectSummaries().stream().mapToLong(S3ObjectSummary::getSize).sum();
		}

		System.out.println("File count: " + fileCount);
		System.out.println("Total file size: " + totalFileSize);

		System.out.println("Files");

		listObjects.getObjectSummaries().stream().forEach((elem) -> {
			System.out.println(elem.getKey());
			System.out.println(elem.getSize());
			System.out.println(elem.getLastModified());
			System.out.println(elem.getStorageClass());
		});
	}
}
