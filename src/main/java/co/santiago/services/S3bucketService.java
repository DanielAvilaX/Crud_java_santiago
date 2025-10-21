package co.santiago.services;

import co.santiago.dto.ItemsDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Map;

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

            // Si quieres mostrarla en formato dd/MM/yyyy
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            String fechaFormateada = fechaActual.format(formatter);

            String json = objectMapper.writeValueAsString(itemsDTO);

            // Definir el key (la "ruta" en el bucket)
            String key = String.format("items/%s/%s.json",fechaFormateada, itemsDTO.getNombre());

            // Subir a S3
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(key)
                            .contentType("application/json")
                            .build(),
                    RequestBody.fromString(json, StandardCharsets.UTF_8)
            );

            log.info("Guardado en S3: " + key);

        } catch (Exception e) {
            throw new RuntimeException("Error al guardar en S3: " + e.getMessage(), e);
        }
    }
}
