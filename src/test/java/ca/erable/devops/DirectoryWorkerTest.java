package ca.erable.devops;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;

@RunWith(MockitoJUnitRunner.class)
public class DirectoryWorkerTest {

    @Mock
    private AmazonS3 client;

    @Test
    public void givenNoNewPrefix_thenHasPrefixesFalse() {

        String prefix = "pre";

        ObjectListing value = new ObjectListing();
        value.setCommonPrefixes(new ArrayList<>());
        Mockito.when(client.listObjects(Mockito.any(ListObjectsRequest.class))).thenReturn(value);

        DirectoryWorker directoryWorker = new DirectoryWorker(prefix, client, "", null);
        directoryWorker.call();
        assertFalse(directoryWorker.hasPrefixes());
    }

    @Test
    public void givenNewPrefix_thenHasPrefixesTrue() {
        String prefix = "pre";
        ObjectListing value = new ObjectListing();
        ArrayList<String> commonPrefixes = new ArrayList<>();
        commonPrefixes.add("prefix");
        value.setCommonPrefixes(commonPrefixes);
        Mockito.when(client.listObjects(Mockito.any(ListObjectsRequest.class))).thenReturn(value);
        DirectoryWorker directoryWorker = new DirectoryWorker(prefix, client, "", null);
        directoryWorker.call();
        assertTrue(directoryWorker.hasPrefixes());
    }

    @Test
    public void givenDirectoryHasOneFiles_thenReturnOne() {
        String prefix = "pre";
        ObjectListing value = new ObjectListing();

        S3ObjectSummary s3ObjectSummary = new S3ObjectSummary();
        s3ObjectSummary.setSize(16L);
        value.getObjectSummaries().add(s3ObjectSummary);

        Mockito.when(client.listObjects(Mockito.any(ListObjectsRequest.class))).thenReturn(value);
        DirectoryWorker directoryWorker = new DirectoryWorker(prefix, client, "", null);
        directoryWorker.call();
        DirectoryResult result = directoryWorker.getResult();
        assertTrue(result.getFileCount() == 1);
    }

    @Test
    public void givenDirectoryHasNoFile_thenReturnZero() {
        String prefix = "pre";
        ObjectListing value = new ObjectListing();

        Mockito.when(client.listObjects(Mockito.any(ListObjectsRequest.class))).thenReturn(value);
        DirectoryWorker directoryWorker = new DirectoryWorker(prefix, client, "", null);
        directoryWorker.call();
        DirectoryResult result = directoryWorker.getResult();
        assertTrue(result.getFileCount() == 0);
    }

    @Test
    public void givenTruncated_thenCallListTwoTime() {

        String prefix = "pre";
        ObjectListing notTruncated = new ObjectListing();
        notTruncated.setTruncated(false);

        ObjectListing truncated = new ObjectListing();
        truncated.setTruncated(true);

        S3ObjectSummary s3ObjectSummary = new S3ObjectSummary();
        s3ObjectSummary.setSize(16L);

        truncated.getObjectSummaries().add(s3ObjectSummary);
        notTruncated.getObjectSummaries().add(s3ObjectSummary);

        Mockito.when(client.listObjects(Mockito.any(ListObjectsRequest.class))).thenReturn(truncated);

        Mockito.when(client.listNextBatchOfObjects(truncated)).thenReturn(notTruncated);

        DirectoryWorker directoryWorker = new DirectoryWorker(prefix, client, "", null);
        directoryWorker.call();

        Mockito.verify(client).listNextBatchOfObjects(Mockito.any(ObjectListing.class));

        DirectoryResult result = directoryWorker.getResult();

        assertTrue(2 == result.getFileCount());
        assertTrue(32 == result.getFileSize());
    }
}
