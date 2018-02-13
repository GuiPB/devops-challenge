package ca.erable.devops;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.model.Bucket;

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
        // Appliquer le filtre de nom optionnel.
        bucketList = awsS3.listBuckets().stream().filter(bucket -> bucketNameMatches.test(bucket.getName())).collect(Collectors.toList());

        for (Bucket bucket : bucketList) {
            Regions bucketLocation = awsS3.getBucketLocation(bucket.getName());

            BucketReport bucketReport = awsS3.reportOnBucket(bucket.getName());

            reports.add(bucketReport);
        }
    }

    public void analyseBuckets(StorageFilter storageFilter) {
        byStorage = storageFilter;
        analyse();
    }

    public void analyseBuckets(String pattern) {
        bucketNameMatches = Pattern.compile(pattern).asPredicate();
        analyse();
    }

    public void analyseBuckets(StorageFilter storageFilter, String pattern) {
        byStorage = storageFilter;
        bucketNameMatches = Pattern.compile(pattern).asPredicate();
        analyse();
    }

    public List<BucketReport> getAllReports() {
        return Collections.unmodifiableList(reports);
    }
}
