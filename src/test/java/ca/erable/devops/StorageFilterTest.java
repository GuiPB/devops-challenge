package ca.erable.devops;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.model.StorageClass;

public class StorageFilterTest {

    @Test
    public void givenGlacierFiltered_thenReturnTrue() {
        S3ObjectSummary objSummary = new S3ObjectSummary();
        objSummary.setStorageClass(StorageClass.Glacier.toString());
        assertTrue(StorageFilter.GLACIER.isFiltred(objSummary));
    }

    @Test
    public void givenGlacierNotFiltered_thenReturnFalse() {
        S3ObjectSummary objSummary = new S3ObjectSummary();
        objSummary.setStorageClass(StorageClass.ReducedRedundancy.toString());
        assertFalse(StorageFilter.GLACIER.isFiltred(objSummary));
    }

    @Test
    public void givenStandardFiltered_thenReturnTrue() {
        S3ObjectSummary objSummary = new S3ObjectSummary();
        objSummary.setStorageClass(StorageClass.Standard.toString());
        assertTrue(StorageFilter.STANDARD.isFiltred(objSummary));
    }

    @Test
    public void givenStandardNotFiltered_thenReturnFalse() {
        S3ObjectSummary objSummary = new S3ObjectSummary();
        objSummary.setStorageClass(StorageClass.Glacier.toString());
        assertFalse(StorageFilter.STANDARD.isFiltred(objSummary));
    }

    @Test
    public void givenStandardIAFiltered_thenReturnTrue() {
        S3ObjectSummary objSummary = new S3ObjectSummary();
        objSummary.setStorageClass(StorageClass.StandardInfrequentAccess.toString());
        assertTrue(StorageFilter.STANDARD_IA.isFiltred(objSummary));
    }

    @Test
    public void givenStandardIANotFiltered_thenReturnFalse() {
        S3ObjectSummary objSummary = new S3ObjectSummary();
        objSummary.setStorageClass(StorageClass.Standard.toString());
        assertFalse(StorageFilter.STANDARD_IA.isFiltred(objSummary));
    }

    @Test
    public void givenRRFiltered_thenReturnTrue() {
        S3ObjectSummary objSummary = new S3ObjectSummary();
        objSummary.setStorageClass(StorageClass.ReducedRedundancy.toString());
        assertTrue(StorageFilter.REDUCED_REDUNDANCY.isFiltred(objSummary));
    }

    @Test
    public void givenRRNotFiltered_thenReturnFalse() {
        S3ObjectSummary objSummary = new S3ObjectSummary();
        objSummary.setStorageClass(StorageClass.Standard.toString());
        assertFalse(StorageFilter.STANDARD_IA.isFiltred(objSummary));
    }

    @Test
    public void givenNoFilter_thenReturnTrue() {
        S3ObjectSummary objSummary = new S3ObjectSummary();
        objSummary.setStorageClass(StorageClass.Standard.toString());
        assertTrue(StorageFilter.NO_FILTER.isFiltred(objSummary));
    }

}
