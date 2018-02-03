package ca.erable.coveo;

public class Main {

	public static void main(String[] args) {
		AmazonS3Service service = new AmazonS3ServiceImpl();
		BucketsAnalyser analyser = new BucketsAnalyser(service);

		analyser.analyse();

		BucketReport report = analyser.report("ca.erable.boisclair");

		System.out.println("Bucket name: " + report.getName());
		System.out.println("Bucket creation date: " + report.getCreationDate());
		System.out.println("Bucket last modified: " + report.getLastModifiedDate());
		System.out.println("Bucket file count: " + report.fileCount());
		System.out.println("Bucket size: " + report.totalFileSize());
	}
}
