package ca.erable.devops;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.junit.Test;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.model.S3ObjectSummary;

public class BucketReportTest {

    @Test
    public void givenBucketContainsTwoFile_thenReturnTwoFileCount() {

        List<S3ObjectSummary> objects = new ArrayList<>();
        S3ObjectSummary s3ObjectSummary = new S3ObjectSummary();
        s3ObjectSummary.setLastModified(new Date());
        objects.add(s3ObjectSummary);
        objects.add(s3ObjectSummary);

        BucketReport report = new BucketReport("erable", new Date(), Regions.DEFAULT_REGION.toString(), 2, null, null);

        assertTrue(2 == report.getFileCount());
    }

    @Test
    public void givenBucketContainsThreeFiles_thenReturnThreeFileCount() {
        List<S3ObjectSummary> objects = new ArrayList<>();
        S3ObjectSummary s3ObjectSummary = new S3ObjectSummary();
        s3ObjectSummary.setLastModified(new Date());
        objects.add(s3ObjectSummary);
        objects.add(s3ObjectSummary);
        objects.add(s3ObjectSummary);

        BucketReport report = new BucketReport("name", new Date(), Regions.DEFAULT_REGION.toString(), objects.size(), null, null);

        assertTrue(3 == report.getFileCount());
    }

    @Test
    public void givenBucketHasNoFiles_thenReturnCountZero() {

        BucketReport report = new BucketReport("eralbe", new Date(), Regions.DEFAULT_REGION.toString(), 0, null, null);

        assertTrue(0L == report.getFileCount());
    }

    @Test
    public void givenABucket_thenReportCreationDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(2012, Calendar.JANUARY, 1);

        Date simulatedCreationTime = calendar.getTime();

        BucketReport rep = new BucketReport("name", simulatedCreationTime, Regions.DEFAULT_REGION.toString(), null, null, null);

        assertTrue(simulatedCreationTime.getTime() == rep.getCreationDate().getTime());
    }

    @Test
    public void givenThreeTime300k_thenReportSum() {
        ArrayList<S3ObjectSummary> returnedObject = new ArrayList<S3ObjectSummary>();

        S3ObjectSummary firstObject = new S3ObjectSummary();
        firstObject.setSize(300);
        firstObject.setLastModified(new Date());

        returnedObject.add(firstObject);

        S3ObjectSummary secondObject = new S3ObjectSummary();
        secondObject.setSize(300);
        secondObject.setLastModified(new Date());

        returnedObject.add(secondObject);

        BucketReport rep = new BucketReport("name", new Date(), Regions.DEFAULT_REGION.toString(), null, 600L, null);
        assertEquals(new Long(600), rep.getTotalFileSize());
    }

    @Test
    public void givenHumanReadable_thenReturnHumanReadable() {
        BucketReport report = new BucketReport("", null, Regions.DEFAULT_REGION.toString(), null, null, null);

        assertEquals("12,9 MB", report.toReadableFileSize(12875897L));
        assertEquals("999 B", report.toReadableFileSize(999L));
        assertEquals("250 B", report.toReadableFileSize(250L));
        assertEquals("2,5 kB", report.toReadableFileSize(2500L));
    }
}
