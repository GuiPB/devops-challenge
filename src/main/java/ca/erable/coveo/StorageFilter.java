package ca.erable.coveo;

import java.util.function.Function;

import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.model.StorageClass;

public enum StorageFilter {
	STANDARD((o) -> o.equals(StorageClass.Standard.toString())), 
	STANDARD_IA((o) -> o.equals(StorageClass.StandardInfrequentAccess.toString())), 
	REDUCED_REDUNDANCY((o) -> o.equals(StorageClass.ReducedRedundancy.toString())), 
	GLACIER((o) -> o.equals(StorageClass.Glacier.toString())), 
	NO_FILTER((o) -> o.equals(o));

	private Function<String, Boolean> filter;

	private StorageFilter(Function<String, Boolean> fct) {
		filter = fct;
	}

	public boolean isFiltred(S3ObjectSummary objSummary) {
		return filter.apply(objSummary.getStorageClass());
	}

}
