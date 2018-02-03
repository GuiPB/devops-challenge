package ca.erable.coveo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.Owner;

@RunWith(MockitoJUnitRunner.class)
public class BucketsAnalyserTest {

	@Mock
	private AmazonS3Service s3Service;

	@Test
	public void givenOneBucket_thenContainsBucketTrue() {
		String first = "ca.erable.boisclair";

		List<Bucket> expectedBuckets = Arrays.asList(new Bucket[] { new Bucket(first) });

		Mockito.when(s3Service.listBuckets()).thenReturn(expectedBuckets);

		BucketsAnalyser report = new BucketsAnalyser(s3Service);
		report.analyse();

		assertTrue(report.containsBucket(first));
	}

	@Test
	public void givenMultipleBucket_thenContainsAllTrue() {
		String first = "ca.erable.boisclair";
		String second = "ca.erable.boisclair2";

		List<Bucket> expectedBuckets = Arrays.asList(new Bucket[] { new Bucket(first), new Bucket(second) });

		Mockito.when(s3Service.listBuckets()).thenReturn(expectedBuckets);

		BucketsAnalyser report = new BucketsAnalyser(s3Service);
		report.analyse();

		expectedBuckets.stream().forEach(elem -> assertTrue(report.containsBucket(elem.getName())));
	}

	@Test
	public void givenBucketDoesNotExist_thenReturnFalse() {
		String nonExistent = "nonExistent";

		List<Bucket> expectedBuckets = Arrays
				.asList(new Bucket[] { new Bucket("ca.erable"), new Bucket("ca.erable2") });

		Mockito.when(s3Service.listBuckets()).thenReturn(expectedBuckets);

		BucketsAnalyser report = new BucketsAnalyser(s3Service);
		report.analyse();
		assertFalse(report.containsBucket(nonExistent));
	}

	@Test
	public void givenABucketExists_thenReturnReport() {
		String bucketName = "ca.erable.boisclair";

		Bucket simulatedBucket = new Bucket(bucketName);
		simulatedBucket.setOwner(new Owner("1", "guillaume"));

		BucketReport simulatedReport = new BucketReport(bucketName, null, new ArrayList<>());

		Mockito.when(s3Service.listBuckets()).thenReturn(Arrays.asList(simulatedBucket));

		BucketsAnalyser analyser = new BucketsAnalyser(s3Service);
		analyser.analyse();

		BucketReport actualReport = analyser.report(bucketName);

		assertFalse(actualReport == null);
		assertEquals(simulatedReport, actualReport);
	}

	@Test
	public void givenABucketDoesNotExist_thenReturnEmptyReport() {
		String bucketName = "ca.erable.boisclair";

		Mockito.when(s3Service.listBuckets()).thenReturn(Arrays.asList(new Bucket(bucketName)));

		BucketsAnalyser analyser = new BucketsAnalyser(s3Service);
		analyser.analyse();
		BucketReport actualReport = analyser.report("");

		assertTrue(actualReport == null);
	}
}
