package ca.erable.devops;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.model.StorageClass;

@RunWith(MockitoJUnitRunner.class)
public class DirectoryWorkerTest {

    @Mock
    private AmazonS3 client;

    @Test(expected = IllegalArgumentException.class)
    public void givenWrongParams_thenThrow() {
        DirectoryWorker directoryWorker = new DirectoryWorker("", client, "", null);
    }

    @Test
    public void givenNoNewPrefix_thenHasPrefixesFalse() {

        String prefix = "pre";

        ObjectListing value = new ObjectListing();
        value.setCommonPrefixes(new ArrayList<>());
        S3ObjectSummary s3ObjectSummary = new S3ObjectSummary();
        s3ObjectSummary.setKey("key");
        s3ObjectSummary.setLastModified(new Date());
        s3ObjectSummary.setStorageClass(StorageClass.Glacier.toString());
        value.getObjectSummaries().add(s3ObjectSummary);
        Mockito.when(client.listObjects(Mockito.any(ListObjectsRequest.class))).thenReturn(value);

        DirectoryWorker directoryWorker = new DirectoryWorker(prefix, client, "notEmpty", StorageFilter.NO_FILTER);
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
        DirectoryWorker directoryWorker = new DirectoryWorker(prefix, client, "test", StorageFilter.NO_FILTER);
        directoryWorker.call();
        assertTrue(directoryWorker.hasPrefixes());
    }

    @Test
    public void givenDirectoryHasOneFiles_thenReturnOne() {
        String prefix = "pre";
        ObjectListing value = new ObjectListing();

        S3ObjectSummary s3ObjectSummary = new S3ObjectSummary();
        s3ObjectSummary.setLastModified(new Date());
        s3ObjectSummary.setSize(16L);
        s3ObjectSummary.setStorageClass(StorageClass.Standard.toString());
        s3ObjectSummary.setKey("key");
        value.getObjectSummaries().add(s3ObjectSummary);

        Mockito.when(client.listObjects(Mockito.any(ListObjectsRequest.class))).thenReturn(value);
        DirectoryWorker directoryWorker = new DirectoryWorker(prefix, client, "test", StorageFilter.NO_FILTER);
        directoryWorker.call();
        DirectoryResult result = directoryWorker.getResult();
        assertTrue(result.getFileCount() == 1);
    }

    @Test
    public void givenDirectoryHasNoFile_thenReturnZero() {
        String prefix = "pre";
        ObjectListing value = new ObjectListing();

        Mockito.when(client.listObjects(Mockito.any(ListObjectsRequest.class))).thenReturn(value);
        DirectoryWorker directoryWorker = new DirectoryWorker(prefix, client, "test", StorageFilter.NO_FILTER);
        directoryWorker.call();
        DirectoryResult result = directoryWorker.getResult();
        assertTrue(result.getFileCount() == 0);
    }

    @Test
    public void givenGlacierFilterApplied_thenReturnZero() {

        String prefix = "pre";
        ObjectListing notTruncated = new ObjectListing();
        notTruncated.setTruncated(false);

        S3ObjectSummary s3ObjectSummary = new S3ObjectSummary();
        s3ObjectSummary.setLastModified(new Date());
        s3ObjectSummary.setSize(16L);
        s3ObjectSummary.setStorageClass(StorageClass.Standard.toString());

        notTruncated.getObjectSummaries().add(s3ObjectSummary);

        Mockito.when(client.listObjects(Mockito.any(ListObjectsRequest.class))).thenReturn(notTruncated);

        DirectoryWorker directoryWorker = new DirectoryWorker(prefix, client, "test", StorageFilter.GLACIER);
        directoryWorker.call();

        DirectoryResult result = directoryWorker.getResult();

        assertTrue(0 == result.getFileCount());
    }

    @Test
    public void givenGlacierFilterApplied_thenReturnAfterTrucation() {
        String prefix = "pre";
        ObjectListing notTruncated = new ObjectListing();
        notTruncated.setTruncated(false);

        ObjectListing truncated = new ObjectListing();
        truncated.setTruncated(true);

        S3ObjectSummary s3ObjectSummary = new S3ObjectSummary();
        s3ObjectSummary.setLastModified(new Date());
        s3ObjectSummary.setSize(16L);
        s3ObjectSummary.setStorageClass(StorageClass.Standard.toString());

        truncated.getObjectSummaries().add(s3ObjectSummary);
        notTruncated.getObjectSummaries().add(s3ObjectSummary);

        Mockito.when(client.listObjects(Mockito.any(ListObjectsRequest.class))).thenReturn(truncated);

        Mockito.when(client.listNextBatchOfObjects(truncated)).thenReturn(notTruncated);

        DirectoryWorker directoryWorker = new DirectoryWorker(prefix, client, "test", StorageFilter.GLACIER);
        directoryWorker.call();

        Mockito.verify(client).listNextBatchOfObjects(Mockito.any(ObjectListing.class));

        DirectoryResult result = directoryWorker.getResult();

        assertTrue(0 == result.getFileCount());
    }

    @Test
    public void givenTruncated_thenCallListTwoTime() {

        String prefix = "pre";
        ObjectListing notTruncated = new ObjectListing();
        notTruncated.setTruncated(false);

        ObjectListing truncated = new ObjectListing();
        truncated.setTruncated(true);

        S3ObjectSummary s3ObjectSummary = new S3ObjectSummary();
        s3ObjectSummary.setLastModified(new Date());
        s3ObjectSummary.setSize(16L);
        s3ObjectSummary.setStorageClass(StorageClass.Standard.toString());
        s3ObjectSummary.setKey("key");

        truncated.getObjectSummaries().add(s3ObjectSummary);
        notTruncated.getObjectSummaries().add(s3ObjectSummary);

        Mockito.when(client.listObjects(Mockito.any(ListObjectsRequest.class))).thenReturn(truncated);

        Mockito.when(client.listNextBatchOfObjects(truncated)).thenReturn(notTruncated);

        DirectoryWorker directoryWorker = new DirectoryWorker(prefix, client, "test", StorageFilter.NO_FILTER);
        directoryWorker.call();

        Mockito.verify(client).listNextBatchOfObjects(Mockito.any(ObjectListing.class));

        DirectoryResult result = directoryWorker.getResult();

        assertTrue(2 == result.getFileCount());
        assertTrue(32 == result.getFileSize());
    }

    @Test
    public void givenMultipleFiles_thenReturnLastModifiedForBucket() {

        Calendar instance = Calendar.getInstance();
        instance.set(2000, Calendar.JANUARY, 1);
        Date januaryFirst2000 = instance.getTime();

        instance.set(2000, Calendar.JANUARY, 2);
        Date januarySecond2000 = instance.getTime();

        S3ObjectSummary youngestObject = new S3ObjectSummary();
        youngestObject.setLastModified(januarySecond2000);
        youngestObject.setStorageClass(StorageClass.Glacier.toString());
        youngestObject.setKey("key");

        S3ObjectSummary oldestObject = new S3ObjectSummary();
        oldestObject.setLastModified(januaryFirst2000);
        oldestObject.setStorageClass(StorageClass.Glacier.toString());
        oldestObject.setKey("key");

        ObjectListing value = new ObjectListing();

        value.getObjectSummaries().add(youngestObject);
        value.getObjectSummaries().add(oldestObject);

        Mockito.when(client.listObjects(Mockito.any(ListObjectsRequest.class))).thenReturn(value);

        DirectoryWorker worker = new DirectoryWorker("pref", client, "test", StorageFilter.NO_FILTER);
        worker.call();

        assertEquals(januarySecond2000, worker.getLastModifiedDate());
    }

    @Test
    public void givenMultipleFilesAndListingtruncated_thenReturnLastModifiedForBucket() {
        Calendar instance = Calendar.getInstance();
        instance.set(2000, Calendar.JANUARY, 1);
        Date januaryFirst2000 = instance.getTime();

        instance.set(2000, Calendar.JANUARY, 2);
        Date januarySecond2000 = instance.getTime();

        S3ObjectSummary youngestObject = new S3ObjectSummary();
        youngestObject.setLastModified(januarySecond2000);
        youngestObject.setStorageClass(StorageClass.Glacier.toString());
        youngestObject.setKey("key");

        S3ObjectSummary oldestObject = new S3ObjectSummary();
        oldestObject.setLastModified(januaryFirst2000);
        oldestObject.setStorageClass(StorageClass.Glacier.toString());
        oldestObject.setKey("key");

        ObjectListing truncated = new ObjectListing();

        truncated.getObjectSummaries().add(oldestObject);
        truncated.setTruncated(true);

        ObjectListing notTruncated = new ObjectListing();
        notTruncated.getObjectSummaries().add(youngestObject);

        Mockito.when(client.listObjects(Mockito.any(ListObjectsRequest.class))).thenReturn(truncated);
        Mockito.when(client.listNextBatchOfObjects(Mockito.any(ObjectListing.class))).thenReturn(notTruncated);

        DirectoryWorker worker = new DirectoryWorker("pref", client, "test", StorageFilter.NO_FILTER);
        worker.call();

        assertEquals(januarySecond2000, worker.getLastModifiedDate());
    }

    @Test
    public void givenListContainsDirectory_thenFilterItOutFromCount() {

        String prefix = "pre";
        ObjectListing notTruncated = new ObjectListing();
        notTruncated.setTruncated(false);

        ObjectListing truncated = new ObjectListing();
        truncated.setTruncated(true);

        S3ObjectSummary s3ObjectSummary = new S3ObjectSummary();
        s3ObjectSummary.setLastModified(new Date());
        s3ObjectSummary.setSize(16L);
        s3ObjectSummary.setKey("key");
        s3ObjectSummary.setStorageClass(StorageClass.Standard.toString());

        S3ObjectSummary directory = new S3ObjectSummary();
        directory.setKey("dossier/");
        directory.setLastModified(new Date());
        directory.setSize(16L);
        directory.setStorageClass(StorageClass.Standard.toString());

        truncated.getObjectSummaries().add(s3ObjectSummary);
        truncated.getObjectSummaries().add(directory);
        notTruncated.getObjectSummaries().add(s3ObjectSummary);
        notTruncated.getObjectSummaries().add(directory);

        Mockito.when(client.listObjects(Mockito.any(ListObjectsRequest.class))).thenReturn(truncated);

        Mockito.when(client.listNextBatchOfObjects(truncated)).thenReturn(notTruncated);

        DirectoryWorker directoryWorker = new DirectoryWorker(prefix, client, "test", StorageFilter.NO_FILTER);
        directoryWorker.call();

        Mockito.verify(client).listNextBatchOfObjects(Mockito.any(ObjectListing.class));

        DirectoryResult result = directoryWorker.getResult();

        assertTrue(2 == result.getFileCount());
        assertTrue(32 == result.getFileSize());
    }
}
