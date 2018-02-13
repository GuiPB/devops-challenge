package ca.erable.devops;

import java.util.List;
import java.util.concurrent.Callable;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;

public class DirectoryWorker implements Callable<DirectoryResult> {

    private String prefix;
    private AmazonS3 client;
    private String bucket;
    private StorageFilter filter;
    private List<String> commonPrefixes;
    private Long totalFileSize;
    private Integer fileCount;

    public DirectoryWorker(String prefix, AmazonS3 client, String bucket, StorageFilter filterBy) {
        this.prefix = prefix;
        this.client = client;
        this.bucket = bucket;
        this.filter = filterBy;
    }

    public boolean hasPrefixes() {
        return !commonPrefixes.isEmpty();
    }

    public DirectoryResult getResult() {
        return new DirectoryResult(fileCount, totalFileSize, commonPrefixes);
    }

    @Override
    public DirectoryResult call() {
        ObjectListing listObjects = client.listObjects(new ListObjectsRequest(bucket, prefix, null, "/", null));
        commonPrefixes = listObjects.getCommonPrefixes();

        totalFileSize = new Long(0);
        fileCount = new Integer(0);

        List<S3ObjectSummary> objectSummaries = listObjects.getObjectSummaries();
        fileCount += objectSummaries.size();
        totalFileSize += objectSummaries.stream().mapToLong(S3ObjectSummary::getSize).sum();

        while (listObjects.isTruncated()) {
            listObjects = client.listNextBatchOfObjects(listObjects);
            fileCount += listObjects.getObjectSummaries().size();
            totalFileSize += listObjects.getObjectSummaries().stream().mapToLong(S3ObjectSummary::getSize).sum();
        }

        return getResult();
    }
}
