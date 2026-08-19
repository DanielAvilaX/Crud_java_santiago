package co.santiago.services;

import co.santiago.models.Item;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.nio.charset.StandardCharsets;

@Slf4j
public class S3bucketService {

    @Autowired
    private S3Client s3Client;

    @Autowired
    private ObjectMapper objectMapper;

    private String bucketName;

    public S3bucketService(String bucketName) {
        this.bucketName = bucketName;
    }

    public void saveItem(Item item) {
        try {

            String json =
                    objectMapper.writeValueAsString(item);

            String key = String.format(
                    "items/%s.json",
                    item.getId()
            );

            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(key)
                            .contentType("application/json")
                            .build(),
                    RequestBody.fromString(
                            json,
                            StandardCharsets.UTF_8
                    )
            );

            log.info("Guardado en S3/MinIO: {}", key);

        } catch (Exception e) {
            throw new RuntimeException(
                    "Error al guardar item: " + e.getMessage(),
                    e
            );
        }
    }

    public void deleteItem(Long id) {
        try {

            String key = String.format(
                    "items/%s.json",
                    id
            );

            s3Client.deleteObject(
                    builder -> builder
                            .bucket(bucketName)
                            .key(key)
            );

            log.info("Item eliminado de S3/MinIO: {}", key);

        } catch (Exception e) {
            throw new RuntimeException(
                    "Error al eliminar item: " + e.getMessage(),
                    e
            );
        }
    }

    public String getItem(Long id) {
        try {

            String key = String.format(
                    "items/%s.json",
                    id
            );

            String json = s3Client
                    .getObjectAsBytes(
                            builder -> builder
                                    .bucket(bucketName)
                                    .key(key)
                    )
                    .asUtf8String();

            log.info("Item obtenido: {}", key);

            return json;

        } catch (Exception e) {
            throw new RuntimeException(
                    "Error al obtener item: " + e.getMessage(),
                    e
            );
        }
    }
}