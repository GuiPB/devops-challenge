package ca.erable.devops;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.Owner;

@RunWith(MockitoJUnitRunner.class)
public class BucketsAnalyserTest {

    @Mock
    private AmazonS3Service s3Service;

    @Test
    public void givenOneBucket_thenContainsBucketTrue() throws InterruptedException {
        String first = "ca.erable.boisclair";

        List<Bucket> expectedBuckets = Arrays.asList(new Bucket[] { new Bucket(first) });

        Mockito.when(s3Service.listBuckets()).thenReturn(expectedBuckets);

        BucketsAnalyser report = new BucketsAnalyser(s3Service);
        report.analyse();

        assertTrue(report.containsBucket(first));
    }

    @Test
    public void givenMultipleBucket_thenContainsAllTrue() throws InterruptedException {
        String first = "ca.erable.boisclair";
        String second = "ca.erable.boisclair2";

        List<Bucket> expectedBuckets = Arrays.asList(new Bucket[] { new Bucket(first), new Bucket(second) });

        Mockito.when(s3Service.listBuckets()).thenReturn(expectedBuckets);

        BucketsAnalyser report = new BucketsAnalyser(s3Service);
        report.analyse();

        expectedBuckets.stream().forEach(elem -> assertTrue(report.containsBucket(elem.getName())));
    }

    @Test
    public void givenBucketDoesNotExist_thenReturnFalse() throws InterruptedException {
        String nonExistent = "nonExistent";

        List<Bucket> expectedBuckets = Arrays.asList(new Bucket[] { new Bucket("ca.erable"), new Bucket("ca.erable2") });

        Mockito.when(s3Service.listBuckets()).thenReturn(expectedBuckets);

        BucketsAnalyser report = new BucketsAnalyser(s3Service);
        report.analyse();
        assertFalse(report.containsBucket(nonExistent));
    }

    @Test
    public void givenABucketExists_thenReturnReport() throws InterruptedException {
        String bucketName = "ca.erable.boisclair";

        Bucket simulatedBucket = new Bucket(bucketName);
        simulatedBucket.setOwner(new Owner("1", "guillaume"));

        BucketReport simulatedReport = new BucketReport(bucketName, null, Regions.DEFAULT_REGION, null, null, null);

        Mockito.when(s3Service.listBuckets()).thenReturn(Arrays.asList(simulatedBucket));
        Mockito.when(s3Service.reportOnBucket(bucketName, StorageFilter.NO_FILTER)).thenReturn(new BucketReport(bucketName, null, null, null, null, null));

        BucketsAnalyser analyser = new BucketsAnalyser(s3Service);
        analyser.analyse();

        BucketReport actualReport = analyser.report(bucketName);

        assertFalse(actualReport == null);
        assertEquals(simulatedReport, actualReport);
    }

    @Test
    public void givenABucketDoesNotExist_thenReturnEmptyReport() throws InterruptedException {
        String bucketName = "ca.erable.boisclair";

        Mockito.when(s3Service.listBuckets()).thenReturn(Arrays.asList(new Bucket(bucketName)));

        BucketsAnalyser analyser = new BucketsAnalyser(s3Service);
        analyser.analyse();
        BucketReport actualReport = analyser.report("");

        assertTrue(actualReport == null);
    }

    @Test
    public void givenAPatternIsProvided_thenListObjetOnlyOnFilteredBucket() throws InterruptedException {
        Mockito.when(s3Service.listBuckets()).thenReturn(Arrays.asList(new Bucket("ca.erable.boisclair"), new Bucket("ca.erable.boisclair2"), new Bucket("ca.era.boisclair")));

        BucketsAnalyser analyser = new BucketsAnalyser(s3Service);
        analyser.analyseBuckets("ca.erable.*");

        Mockito.verify(s3Service, Mockito.never()).reportOnBucket("ca.erab.boisclair");
    }
}
