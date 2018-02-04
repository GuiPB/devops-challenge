package ca.erable.coveo;

import com.amazonaws.regions.Regions;

public class Main {

	public static void main(String[] args) {
		AmazonS3Service service = new AmazonS3ServiceImpl(Regions.US_EAST_2);
		BucketsAnalyser analyser = new BucketsAnalyser(service);

		analyser.analyse("ca.erable.");

		BucketReport report = analyser.report("ca.erable.boisclair");

		System.out.println("Bucket name: " + report.getName());
		System.out.println("Bucket creation date: " + report.getCreationDate());
		System.out.println("Bucket last modified: " + report.getLastModifiedDate());
		System.out.println("Bucket file count: " + report.getFileCount());
		System.out.println("Bucket size: " + report.toReadableFileSize());
	}
}
