package ca.erable.devops;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.amazonaws.services.s3.model.Bucket;

/**
 * This class orchestrates bucket analysis. By invoking any of the
 * analyseBuckets() methods, it will store a {@link BucketReport}.
 * 
 * @author guillaume
 *
 */
public class BucketsAnalyser {

    private AmazonS3Service awsS3;

    private Map<String, BucketReport> reports = new HashMap<>();
    private List<Bucket> bucketList = new ArrayList<>();

    private StorageFilter byStorage = StorageFilter.NO_FILTER;

    private Predicate<String> bucketNameMatches = Pattern.compile(".*").asPredicate();;

    public BucketsAnalyser(AmazonS3Service service) {
        awsS3 = service;
    }

    /**
     * Retourne vrai ou faux si un bucket est existant
     * 
     * @param matches
     * @return true si contient le bucket, false sinon
     */
    public boolean containsBucket(String matches) {
        return bucketList.stream().anyMatch(e -> matches.equals(e.getName()));
    }

    /**
     * 
     * @param bucketName
     *            to get
     * @return the {@link BucketReport}
     */
    public BucketReport getReport(String bucketName) {
        return reports.get(bucketName);
    }

    /**
     * Demarrer l'analyse
     */
    public void analyse() {
        // Appliquer le filtre de nom optionnel.
        bucketList = awsS3.listBuckets().stream().filter(bucket -> bucketNameMatches.test(bucket.getName())).collect(Collectors.toList());

        for (Bucket bucket : bucketList) {
            BucketReport bucketReport = awsS3.reportOnBucket(bucket.getName(), byStorage);

            reports.put(bucket.getName(), bucketReport);
        }
    }

    /**
     * Demarrer l'analyse selon un filtre de classe de persistance
     * 
     * @param storageFilter
     */
    public void analyseBuckets(StorageFilter storageFilter) {
        byStorage = storageFilter;
        analyse();
    }

    /**
     * Demarrer un analyse sur les bucket repondant a une expression reguliere
     * 
     * @param pattern
     */
    public void analyseBuckets(String pattern) {
        bucketNameMatches = Pattern.compile(pattern).asPredicate();
        analyse();
    }

    /**
     * Lance un analyse sur les bucket repondant a une expression reguliere et a une
     * filtre de classe de persistance
     * 
     * @param storageFilter
     * @param pattern
     */
    public void analyseBuckets(StorageFilter storageFilter, String pattern) {
        byStorage = storageFilter;
        bucketNameMatches = Pattern.compile(pattern).asPredicate();
        analyse();
    }

    /**
     *
     * @return Une liste de tous les rapports de bucket
     */
    public List<BucketReport> getAllReports() {
        List<BucketReport> allReports = new ArrayList<>();
        reports.forEach((s, b) -> allReports.add(b));
        return allReports;
    }
}
