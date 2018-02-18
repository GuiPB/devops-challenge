package ca.erable.devops;

import java.util.function.Function;

import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.model.StorageClass;

/**
 * Enum of each storage filter. An enum value will implement isFiltered on a
 * {@link S3ObjectSummary} for list processing. This mecanism allows to choose
 * the right filter on value lookup from a String.
 * 
 * @author guillaume
 *
 */
public enum StorageFilter {
    STANDARD(obj -> obj.equals(StorageClass.Standard.toString())), STANDARD_IA(obj -> obj.equals(StorageClass.StandardInfrequentAccess.toString())), REDUCED_REDUNDANCY(
            obj -> obj.equals(StorageClass.ReducedRedundancy.toString())), GLACIER(obj -> obj.equals(StorageClass.Glacier.toString())), NO_FILTER(obj -> obj.equals(obj));

    private Function<String, Boolean> filter;

    private StorageFilter(Function<String, Boolean> fct) {
        filter = fct;
    }

    public boolean isFiltred(S3ObjectSummary objSummary) {
        return filter.apply(objSummary.getStorageClass());
    }

}
