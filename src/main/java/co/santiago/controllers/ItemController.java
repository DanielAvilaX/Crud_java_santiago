package co.santiago.controllers;

import co.santiago.dto.InvoiceDTO;
import co.santiago.dto.ItemRequestDTO;
import co.santiago.dto.ItemsDTO;
import co.santiago.models.Item;
import co.santiago.services.ItemService;
import co.santiago.services.ItemServiceImpl;
import co.santiago.services.S3bucketService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/items")
public class ItemController {

    private final ItemService itemService;

    public ItemController(
            ItemService itemService
    ) {
        this.itemService = itemService;
    }
    @Operation(
            summary = "Crear producto"
    )
    @PostMapping("/saveitemS3")
    public ResponseEntity<ItemsDTO> saveItem(
            @Valid @RequestBody ItemRequestDTO itemRequestDTO
    ) {
        return ResponseEntity.ok(
                itemService.saveItem(itemRequestDTO)
        );
    }
    @Operation(
            summary = "Mostrar todos los productos"
    )
    @GetMapping
    public ResponseEntity<Page<ItemsDTO>> getAllItems(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(
                itemService.getAllItems(page, size)
        );
    }
    @Operation(
            summary = "Buscar producto por id"
    )
    @GetMapping("/{id}")
    public ResponseEntity<Item> getItemsById(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                itemService.getItemsById(id)
        );
    }@Operation(
            summary = "Actualizar producto"
    )
    @PutMapping("/{id}")
    public ResponseEntity<ItemsDTO> updateItem(
            @PathVariable Long id,
            @Valid @RequestBody ItemRequestDTO itemRequestDTO
    ) {
        return ResponseEntity.ok(
                itemService.updateItem(id, itemRequestDTO)
        );
    }
    @Operation(
        summary = "Eliminar producto"
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteItem(
            @PathVariable Long id
    ) {

        itemService.deleteItem(id);

        return ResponseEntity.ok(
                "Item eliminado correctamente"
        );
    }
}