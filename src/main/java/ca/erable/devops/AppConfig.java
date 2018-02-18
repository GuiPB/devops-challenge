package ca.erable.devops;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.AnonymousAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

@Configuration
@ComponentScan("ca.erable.devops")
public class AppConfig {

    @Bean
    @Profile("!integ")
    public AmazonS3ClientBuilder getClientBuilder() {
        return AmazonS3ClientBuilder.standard().withRegion(Regions.DEFAULT_REGION);
    }

    @Bean
    @Profile("integ")
    public AmazonS3ClientBuilder getDevClientBuilder() {
        return AmazonS3ClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(new AnonymousAWSCredentials()))
                .withEndpointConfiguration(new EndpointConfiguration("http://localhost:8080", Regions.US_EAST_2.toString())).enablePathStyleAccess();
    }
}
