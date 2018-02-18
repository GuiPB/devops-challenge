package ca.erable.devops;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import java.io.File;
import java.net.URI;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.gaul.s3proxy.S3Proxy;
import org.jclouds.ContextBuilder;
import org.jclouds.blobstore.BlobStoreContext;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.AnonymousAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

/**
 * <p>
 * Cette classe de test s'execute si la propriete de JVM "integration.test" est
 * presente. Ceci lance une instance de serveur S3Mock qui utilisera le systeme
 * de fichier pour creer des fichiers dans des bucket et y acceder. On peut
 * employer la proriete integration.port pour configurer le serveur au cas ou le
 * port par defaut ne serait pas disponible.
 * </p>
 * 
 * <p>
 * Exemple:
 * 
 * -Dintegration.test -Dintegration.port=9090
 * </p>
 * 
 * 
 * @author guillaume
 *
 */
public class AmazonS3ServiceIntegrationTest {

    private S3Proxy s3Proxy;
    private AmazonS3 client;
    private AmazonS3ClientBuilder clientBuilder;

    @BeforeClass
    public static void verifyIfIntegrationTest() {
        assumeTrue(System.getProperty("integration.test") != null);
    }

    @Before
    public void setUpS3Mock() throws Exception {
        Properties properties = new Properties();
        properties.setProperty("jclouds.filesystem.basedir", System.getProperty("user.dir") + "/blobstore");

        String port = "";
        if ((port = System.getProperty("integration.port")) == null) {
            port = new String("8081");
        }

        BlobStoreContext context = ContextBuilder.newBuilder("filesystem").credentials("identity", "credential").overrides(properties).build(BlobStoreContext.class);
        s3Proxy = S3Proxy.builder().blobStore(context.getBlobStore()).endpoint(URI.create("http://127.0.0.1:" + port)).build();

        s3Proxy.start();

        clientBuilder = AmazonS3ClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(new AnonymousAWSCredentials()))
                .withEndpointConfiguration(new EndpointConfiguration("http://localhost:" + port, Regions.US_EAST_2.toString())).enablePathStyleAccess();
        client = clientBuilder.build();
        client.createBucket("bucket1");
        client.createBucket("bucket2");

        for (int i = 0; i < 1500; ++i) {
            client.putObject("bucket1", "directory1/file" + i, "hello world");
        }
        client.putObject("bucket1", "directory1/directory2/file2", "hello world");

        for (int i = 0; i < 1500; ++i) {
            client.putObject("bucket2", "file" + i, "hello world");
        }
    }

    @Test
    public void verifyExpectedResults() {

        BucketsAnalyser analyser = new BucketsAnalyser(new AmazonS3ServiceImpl(clientBuilder));
        analyser.analyse();

        BucketReport bucket1 = analyser.getReport("bucket1");

        assertTrue(bucket1.getBucketLocation().equals("US"));
        assertEquals(new Integer(1501), bucket1.getFileCount());
        assertEquals(new Long(16511), bucket1.getTotalFileSize());

        BucketReport bucket2 = analyser.getReport("bucket2");

        assertTrue(bucket2.getBucketLocation().equals("US"));
        assertEquals(new Integer(1500), bucket2.getFileCount());
        assertEquals(new Long(16500), bucket2.getTotalFileSize());
    }

    @After
    public void tearDownS3Mock() throws Exception {
        s3Proxy.stop();
        FileUtils.deleteDirectory(new File(System.getProperty("user.dir") + "/blobstore"));
    }
}
