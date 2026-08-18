package co.santiago.config;

import co.santiago.services.S3bucketService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class S3ServiceConfiguration {

    @Bean
    public S3bucketService itemsS3bucketService(
            @Value("${bucketName:items}") String bucketName
    ) {
        return new S3bucketService(bucketName);
    }
    @Bean
    @Qualifier("itemsCopy")
    public S3bucketService itemsCopyS3bucketService(
            @Value("${bucketNameCopy:itemscopy}") String bucketName
    ) {
        return new S3bucketService(bucketName);
    }
}