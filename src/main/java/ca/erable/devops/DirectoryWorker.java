package ca.erable.devops;

import static java.util.stream.Collectors.toList;

import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;

public class DirectoryWorker implements Callable<DirectoryResult> {

    private static Logger log = LogManager.getLogger(DirectoryResult.class);
    private String prefix;
    private AmazonS3 client;
    private String bucket;
    private StorageFilter filter;
    private List<String> commonPrefixes;
    private Long totalFileSize;
    private Integer fileCount;
    private Date lastModified;

    public DirectoryWorker(String prefix, AmazonS3 client, String bucket, StorageFilter filterBy) {
        if (StringUtils.isBlank(bucket) || StringUtils.isBlank(bucket) || client == null || filterBy == null) {
            throw new IllegalArgumentException("Invalid parameter for worker");
        }
        this.prefix = prefix;
        this.client = client;
        this.bucket = bucket;
        this.filter = filterBy;
    }

    public boolean hasPrefixes() {
        return !commonPrefixes.isEmpty();
    }

    public DirectoryResult getResult() {
        log.debug(() -> "DirectoryWorker results on prefix [" + prefix + "] in bucket " + bucket + " filecount: " + fileCount + " totalFileSize: " + totalFileSize + " commonPrefixes: "
                + String.join(",", commonPrefixes));
        return new DirectoryResult(fileCount, totalFileSize, commonPrefixes, lastModified);
    }

    @Override
    public DirectoryResult call() {
        log.debug(() -> "DirectoryWorker on prefix [" + prefix + "] in bucket " + bucket);
        ObjectListing listObjects = client.listObjects(new ListObjectsRequest(bucket, prefix, null, "/", null));
        commonPrefixes = listObjects.getCommonPrefixes();

        totalFileSize = new Long(0);
        fileCount = new Integer(0);

        List<S3ObjectSummary> objectSummaries = listObjects.getObjectSummaries().stream().filter(y -> filter.isFiltred(y)).filter(i -> !i.getKey().endsWith("/")).collect(toList());

        if (!objectSummaries.isEmpty()) {
            lastModified = objectSummaries.stream().map(S3ObjectSummary::getLastModified).sorted((a, b) -> b.compareTo(a)).findFirst().get();
            fileCount += objectSummaries.size();
            totalFileSize += objectSummaries.stream().mapToLong(S3ObjectSummary::getSize).sum();

        }

        while (listObjects.isTruncated()) {
            log.debug(() -> "DirectoryWorker on prefix [" + prefix + "] in bucket " + bucket + ". Truncation detected. Fetching next batch.");
            listObjects = client.listNextBatchOfObjects(listObjects);
            List<S3ObjectSummary> nextObjectSummaries = listObjects.getObjectSummaries().stream().filter(y -> filter.isFiltred(y)).filter(i -> !i.getKey().endsWith("/")).collect(toList());

            if (!nextObjectSummaries.isEmpty()) {
                Date modified = nextObjectSummaries.stream().map(S3ObjectSummary::getLastModified).sorted((a, b) -> b.compareTo(a)).findFirst().get();
                if (modified != null && lastModified.before(modified)) {
                    lastModified = modified;
                }
                fileCount += nextObjectSummaries.size();
                totalFileSize += nextObjectSummaries.stream().mapToLong(S3ObjectSummary::getSize).sum();
            }

        }

        return getResult();
    }

    public Date getLastModifiedDate() {

        return lastModified;
    }
}
