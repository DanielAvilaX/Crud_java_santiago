package co.santiago.controllers;

import co.santiago.dto.ItemsDTO;
import co.santiago.services.PingService;
import co.santiago.services.S3bucketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ItemController {
    @Autowired
    private S3bucketService s3bucketService;
    @PostMapping("/saveitemS3")
    public ResponseEntity<String> saveItem(
            @RequestBody ItemsDTO itemsDTO
    ) {
        s3bucketService.saveItem(itemsDTO);
        return ResponseEntity.ok(" Item guardado con fecha ");
    }

}
