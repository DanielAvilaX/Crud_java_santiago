package co.santiago.services;

import co.santiago.dto.ItemsDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

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

    public void saveItem(ItemsDTO itemsDTO) {
        try {

            LocalDate fechaActual = LocalDate.now();

            DateTimeFormatter formatter =
                    DateTimeFormatter.ofPattern("dd-MM-yyyy");

            String fechaFormateada =
                    fechaActual.format(formatter);

            String json =
                    objectMapper.writeValueAsString(itemsDTO);

            String key = String.format(
                    "items/%s/%s.json",
                    fechaFormateada,
                    itemsDTO.getNombre()
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
                    "items/%s/%s.json", id
            );

            s3Client.deleteObject(
                    builder -> builder
                            .bucket(bucketName)
                            .key(key)
            );

            log.info("Item eliminado: {}", key);

        } catch (Exception e) {
            throw new RuntimeException(
                    "Error al eliminar item: " + e.getMessage(),
                    e
            );
        }
    }

    public String getItem(String fecha, String nombre) {
        try {

            String key = String.format(
                    "items/%s/%s.json",
                    fecha,
                    nombre
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