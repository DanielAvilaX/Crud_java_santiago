package co.santiago.config;


import co.santiago.services.S3bucketService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class S3ServiceConfiguration {
    @Bean
    public S3bucketService s3bucketService(@Value("${bucketName:santiago-api-us-west-2}") String bucketName){
        return new S3bucketService(bucketName);
    }
}
