package ca.erable.coveo;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.S3ObjectSummary;

public class BucketsAnalyser {

	private AmazonS3Service awsS3;

	private List<BucketReport> reports = new ArrayList<>();
	private List<Bucket> bucketList = new ArrayList<>();

	private StorageFilter byStorage = StorageFilter.NO_FILTER;

	private Predicate<String> bucketNameMatches = Pattern.compile(".*").asPredicate();;

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
		// Appliquer le filtre de nom.
		bucketList = awsS3.listBuckets().stream().filter(bucket -> bucketNameMatches.test(bucket.getName()))
				.collect(Collectors.toList());
		bucketList.stream().forEach(t -> {
			List<S3ObjectSummary> objects = awsS3.listObject(t.getName());
			reports.add(new BucketReport(t.getName(), t.getCreationDate(),
					objects.stream().filter(o -> byStorage.isFiltred(o)).collect(Collectors.toList())));
		});
	}

	/**
	 * Applique un filtre base sur le type de systeme de fichier
	 * 
	 * @param storageFilter
	 */
	public void analyse(StorageFilter storageFilter) {
		byStorage = storageFilter;
		analyse();
	}

	public void analyse(String pattern) {
		bucketNameMatches = Pattern.compile(pattern).asPredicate();
		analyse();
	}
}
