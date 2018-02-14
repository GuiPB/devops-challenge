package ca.erable.devops;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Test;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.AnonymousAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;

// -ea -Daws.accessKeyId=AKIAI7E35P2JWVFTBU6A -Daws.secretKey=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

public class PrimitiveTest {

    @Test
    public void test() {
        AmazonS3 defaultClient = AmazonS3ClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(new AnonymousAWSCredentials()))
                .withEndpointConfiguration(new EndpointConfiguration("http://localhost:8080", Regions.DEFAULT_REGION.toString())).enablePathStyleAccess().build();

        // defaultClient.createBucket("bucket1");

        List<Bucket> listBuckets = defaultClient.listBuckets();

        System.out.println("Buckets");
        listBuckets.stream().forEach((bk) -> {
            System.out.println(bk.getName());
            System.out.println(bk.getCreationDate());
            System.out.println(defaultClient.getBucketLocation(bk.getName()));
        });

        Long totalFileSize = new Long(0);
        Integer fileCount = new Integer(0);

        long start = System.currentTimeMillis();

        ObjectListing listObjects = defaultClient.listObjects(new ListObjectsRequest("bucket1", null, null, "/", null));
        List<String> commonPrefixes = listObjects.getCommonPrefixes();
        System.out.println("Common prefixes: " + commonPrefixes);

        ExecutorService threadPool = Executors.newFixedThreadPool(10);

        commonPrefixes.forEach(prefix -> {
            ObjectListing listObjects2 = defaultClient.listObjects(new ListObjectsRequest("bucket1", prefix, null, "/", null));
            System.out.println(listObjects2.getCommonPrefixes());
        });

        fileCount += listObjects.getObjectSummaries().size();
        totalFileSize += listObjects.getObjectSummaries().parallelStream().mapToLong(S3ObjectSummary::getSize).sum();

        while (listObjects.isTruncated()) {
            listObjects = defaultClient.listNextBatchOfObjects(listObjects);
            fileCount += listObjects.getObjectSummaries().size();
            totalFileSize += listObjects.getObjectSummaries().stream().mapToLong(S3ObjectSummary::getSize).sum();
        }

        System.out.println("Elapsed: " + (System.currentTimeMillis() - start));

        System.out.println("File count: " + fileCount);
        System.out.println("Total file size: " + totalFileSize);

        /*
         * System.out.println("Files");
         * 
         * listObjects.getObjectSummaries().stream().forEach((elem) -> {
         * System.out.println(elem.getKey()); System.out.println(elem.getSize());
         * System.out.println(elem.getLastModified());
         * System.out.println(elem.getStorageClass()); });
         */
    }
}
