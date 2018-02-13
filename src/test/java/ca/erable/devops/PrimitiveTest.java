package ca.erable.devops;

import java.util.List;

import org.junit.Test;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.AnonymousAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.Region;
import com.amazonaws.services.s3.model.S3ObjectSummary;

// -ea -Daws.accessKeyId=AKIAI7E35P2JWVFTBU6A -Daws.secretKey=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

public class PrimitiveTest {

    @Test
    public void test() {
        AmazonS3 defaultClient = AmazonS3ClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(new AnonymousAWSCredentials()))
                .withEndpointConfiguration(new EndpointConfiguration("http://localhost:8080", Region.US_East_2.getFirstRegionId())).enablePathStyleAccess().build();

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

        ObjectListing listObjects = defaultClient.listObjects(new ListObjectsRequest("ca.erable.boisclair", "dossier2/", null, "/", null));
        System.out.println("Common prefixes: " + listObjects.getCommonPrefixes());

        // ExecutorService threadPool = Executors.newFixedThreadPool(10);

        // threadPool.

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
