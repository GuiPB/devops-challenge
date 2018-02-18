package ca.erable.devops;

import java.util.List;

import com.amazonaws.services.s3.model.Bucket;

/**
 * AmazonS3 service interface
 * 
 * @author guillaume
 *
 */
public interface AmazonS3Service {
    /**
     * 
     * @return a list of every bucket owned by the ID and secret key
     */
    List<Bucket> listBuckets();

    /**
     * 
     * @param bucketName
     * @return Location where the bucket is hosted
     */
    String getBucketLocation(String bucketName);

    /**
     * 
     * @param name
     *            of the required bucket
     * 
     * @param byStorage
     *            Filters by storage type. This means that any other storage type
     *            will be ignored
     * @return A {@link BucketReport} by required bucket name
     */
    BucketReport reportOnBucket(String name, StorageFilter byStorage);

    /**
     * 
     * @param name
     *            of the required bucket
     * @return A {@link BucketReport} by required bucket name
     */
    BucketReport reportOnBucket(String name);
}
