package co.santiago.controllers;

import co.santiago.dto.ItemsDTO;
import co.santiago.models.Item;
import co.santiago.services.ItemService;
import co.santiago.services.ItemServiceImpl;
import co.santiago.services.S3bucketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/items")
public class ItemController {

    private final ItemService itemService;

    public ItemController(
            ItemService itemService
    ) {
        this.itemService = itemService;
    }

    @PostMapping("/saveitemS3")
    public ResponseEntity<String> saveItem(
            @RequestBody ItemsDTO itemsDTO
    ) {
        itemService.saveItem(itemsDTO);

        return ResponseEntity.ok(
                "Item guardado correctamente"
        );
    }
    @GetMapping
    public ResponseEntity<Page<ItemsDTO>> getAllItems(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(
                itemService.getAllItems(page, size)
        );
    }
    @PutMapping("/{id}")
    public ResponseEntity<ItemsDTO> updateItem(
            @PathVariable Long id,
            @RequestBody ItemsDTO itemsDTO
    ) {
        return ResponseEntity.ok(
                itemService.updateItem(id, itemsDTO)
        );
    }
}