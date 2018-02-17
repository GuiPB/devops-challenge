package ca.erable.devops;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.amazonaws.services.s3.model.Bucket;

public class BucketsAnalyser {

    private AmazonS3Service awsS3;

    private Map<String, BucketReport> reports = new HashMap<>();
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
        return reports.get(bucketName);
    }

    public void analyse() {
        // Appliquer le filtre de nom optionnel.
        bucketList = awsS3.listBuckets().stream().filter(bucket -> bucketNameMatches.test(bucket.getName())).collect(Collectors.toList());

        for (Bucket bucket : bucketList) {
            BucketReport bucketReport = awsS3.reportOnBucket(bucket.getName(), byStorage);

            reports.put(bucket.getName(), bucketReport);
        }
    }

    public void analyseBuckets(StorageFilter storageFilter) throws InterruptedException {
        byStorage = storageFilter;
        analyse();
    }

    public void analyseBuckets(String pattern) throws InterruptedException {
        bucketNameMatches = Pattern.compile(pattern).asPredicate();
        analyse();
    }

    public void analyseBuckets(StorageFilter storageFilter, String pattern) throws InterruptedException {
        byStorage = storageFilter;
        bucketNameMatches = Pattern.compile(pattern).asPredicate();
        analyse();
    }

    public List<BucketReport> getAllReports() {
        List<BucketReport> allReports = new ArrayList<>();
        reports.forEach((s, b) -> allReports.add(b));
        return allReports;
    }
}
