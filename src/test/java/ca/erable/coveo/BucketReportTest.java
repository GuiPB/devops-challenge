package ca.erable.coveo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.S3ObjectSummary;

@RunWith(MockitoJUnitRunner.class)
public class BucketReportTest {

	@Mock
	private AmazonS3Service s3Service;

	@Test
	public void givenBucketContainsTwoFile_thenReturnTwoFileCount() {

		List<S3ObjectSummary> objects = new ArrayList<>();
		S3ObjectSummary s3ObjectSummary = new S3ObjectSummary();
		s3ObjectSummary.setLastModified(new Date());
		objects.add(s3ObjectSummary);
		objects.add(s3ObjectSummary);

		Mockito.when(s3Service.listObject(Mockito.any(String.class))).thenReturn(objects);
		Mockito.when(s3Service.listBuckets()).thenReturn(Arrays.asList(new Bucket("ca.erable")));

		BucketsAnalyser analyser = new BucketsAnalyser(s3Service);
		analyser.analyse();
		BucketReport report = analyser.report("ca.erable");

		assertTrue(2 == report.fileCount());
	}

	@Test
	public void givenBucketContainsThreeFiles_thenReturnThreeFileCount() {
		List<S3ObjectSummary> objects = new ArrayList<>();
		S3ObjectSummary s3ObjectSummary = new S3ObjectSummary();
		s3ObjectSummary.setLastModified(new Date());
		objects.add(s3ObjectSummary);
		objects.add(s3ObjectSummary);
		objects.add(s3ObjectSummary);

		Mockito.when(s3Service.listObject(Mockito.any(String.class))).thenReturn(objects);
		Mockito.when(s3Service.listBuckets()).thenReturn(Arrays.asList(new Bucket("ca.erable")));

		BucketsAnalyser analyser = new BucketsAnalyser(s3Service);
		analyser.analyse();
		BucketReport report = analyser.report("ca.erable");

		assertTrue(3 == report.fileCount());
	}

	@Test
	public void givenBucketHasNoFiles_thenReturnCountZero() {

		Mockito.when(s3Service.listBuckets()).thenReturn(Arrays.asList(new Bucket("ca.erable")));
		Mockito.when(s3Service.listObject(Mockito.any(String.class))).thenReturn(new ArrayList<S3ObjectSummary>());

		BucketsAnalyser analyser = new BucketsAnalyser(s3Service);
		analyser.analyse();
		BucketReport report = analyser.report("ca.erable");

		assertTrue(0 == report.fileCount());
	}

	@Test
	public void givenABucket_thenReportCreationDate() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(2012, Calendar.JANUARY, 1);

		Date simulatedCreationTime = calendar.getTime();
		Bucket first = new Bucket("name");
		first.setCreationDate(simulatedCreationTime);

		Mockito.when(s3Service.listBuckets()).thenReturn(Arrays.asList(first));
		Mockito.when(s3Service.listObject(Mockito.any(String.class))).thenReturn(new ArrayList<S3ObjectSummary>());

		BucketsAnalyser analyser = new BucketsAnalyser(s3Service);
		analyser.analyse();
		BucketReport rep = analyser.report("name");

		assertTrue(simulatedCreationTime.getTime() == rep.getCreationDate().getTime());
	}

	@Test
	public void givenThreeTime300k_thenReportSum() {
		Bucket first = new Bucket("name");
		Mockito.when(s3Service.listBuckets()).thenReturn(Arrays.asList(first));
		ArrayList<S3ObjectSummary> returnedObject = new ArrayList<S3ObjectSummary>();

		S3ObjectSummary firstObject = new S3ObjectSummary();
		firstObject.setSize(300);
		firstObject.setLastModified(new Date());

		returnedObject.add(firstObject);

		S3ObjectSummary secondObject = new S3ObjectSummary();
		secondObject.setSize(300);
		secondObject.setLastModified(new Date());

		returnedObject.add(secondObject);
		Mockito.when(s3Service.listObject(Mockito.any(String.class))).thenReturn(returnedObject);

		BucketsAnalyser analyser = new BucketsAnalyser(s3Service);
		analyser.analyse();
		BucketReport rep = analyser.report("name");
		assertEquals(new Long(600), rep.totalFileSize());
	}

	@Test
	public void givenMultipleFiles_thenReturnLastModifiedForBucket() {
		Bucket first = new Bucket("name");
		Mockito.when(s3Service.listBuckets()).thenReturn(Arrays.asList(first));
		ArrayList<S3ObjectSummary> returnedObject = new ArrayList<S3ObjectSummary>();

		Calendar instance = Calendar.getInstance();
		instance.set(2000, Calendar.JANUARY, 1);
		Date januaryFirst2000 = instance.getTime();

		instance.set(2000, Calendar.JANUARY, 2);
		Date januarySecond2000 = instance.getTime();

		S3ObjectSummary firstObject = new S3ObjectSummary();
		firstObject.setLastModified(januarySecond2000);
		returnedObject.add(firstObject);

		S3ObjectSummary secondObject = new S3ObjectSummary();
		secondObject.setLastModified(januaryFirst2000);

		returnedObject.add(secondObject);
		Mockito.when(s3Service.listObject(Mockito.any(String.class))).thenReturn(returnedObject);

		BucketsAnalyser analyser = new BucketsAnalyser(s3Service);
		analyser.analyse();
		BucketReport rep = analyser.report("name");

		assertEquals(januarySecond2000, rep.getLastModifiedDate());
	}
}
