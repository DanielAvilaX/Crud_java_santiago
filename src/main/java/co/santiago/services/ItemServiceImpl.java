package co.santiago.services;

import co.santiago.dto.ItemRequestDTO;
import co.santiago.dto.ItemsDTO;
import co.santiago.exceptions.ItemInactiveException;
import co.santiago.exceptions.ItemNotFoundException;
import co.santiago.models.Item;
import co.santiago.repositories.ItemRepositories;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ItemServiceImpl implements ItemService {

    @Autowired
    private ItemRepositories itemRepository;

    @Autowired
    private S3bucketService itemsS3bucketService;

    @Autowired
    @Qualifier("itemsCopy")
    private S3bucketService itemsCopyS3bucketService;

    @Autowired
    private AuditService auditService;

    @Override
    @Transactional
    public ItemsDTO saveItem(ItemRequestDTO itemRequestDTO) {

        Item item = new Item();

        item.setNombre(itemRequestDTO.getNombre());
        item.setDescripcion(itemRequestDTO.getDescripcion());
        item.setPrecio(itemRequestDTO.getPrecio());

        Item savedItem = itemRepository.saveAndFlush(item);

        itemsS3bucketService.saveItem(savedItem);
        itemsCopyS3bucketService.saveItem(savedItem);

        auditService.log(
                "Item",
                savedItem.getId(),
                "CREATE",
                "santiago",
                null,
                savedItem
        );

        return convertToDTO(savedItem);
    }

    @Override
    public Page<ItemsDTO> getAllItems(int page, int size) {

        Pageable pageable = PageRequest.of(page, size);

        Page<Item> items = itemRepository.findByActivoTrue(pageable);

        return items.map(this::convertToDTO);
    }

    @Override
    @Transactional
    public ItemsDTO updateItem(Long id, ItemRequestDTO itemRequestDTO) {

        Item item = itemRepository.findById(id)
                .orElseThrow(() ->
                        new ItemNotFoundException(id)
                );

        if (!item.isActivo()) {
            throw new ItemInactiveException(id);
        };

        Item before = copyItem(item);

        item.setNombre(itemRequestDTO.getNombre());
        item.setDescripcion(itemRequestDTO.getDescripcion());
        item.setPrecio(itemRequestDTO.getPrecio());

        Item updatedItem = itemRepository.saveAndFlush(item);

        itemsS3bucketService.saveItem(updatedItem);
        itemsCopyS3bucketService.saveItem(updatedItem);

        auditService.log(
                "Item",
                updatedItem.getId(),
                "UPDATE",
                "santiago",
                before,
                updatedItem
        );

        return convertToDTO(updatedItem);
    }

    @Override
    @Transactional
    public void deleteItem(Long id) {

        Item item = itemRepository.findById(id)
                .orElseThrow(() ->
                        new ItemNotFoundException(id)
                );

        if (!item.isActivo()) {
            throw new ItemInactiveException(id);
        }

        Item before = copyItem(item);

        item.setActivo(false);

        Item updatedItem = itemRepository.saveAndFlush(item);

        itemsS3bucketService.saveItem(updatedItem);
        itemsCopyS3bucketService.saveItem(updatedItem);

        auditService.log(
                "Item",
                updatedItem.getId(),
                "DELETE",
                "santiago",
                before,
                updatedItem
        );
    }

    private ItemsDTO convertToDTO(Item item) {

        ItemsDTO dto = new ItemsDTO();

        dto.setId(item.getId());
        dto.setNombre(item.getNombre());
        dto.setDescripcion(item.getDescripcion());
        dto.setPrecioFormateado(
                formatPrecio(item.getPrecio())
        );

        return dto;
    }

    private Item copyItem(Item item) {

        Item copy = new Item();

        copy.setId(item.getId());
        copy.setNombre(item.getNombre());
        copy.setDescripcion(item.getDescripcion());
        copy.setPrecio(item.getPrecio());
        copy.setActivo(item.isActivo());

        return copy;
    }

    private String formatPrecio(Integer precio) {
        return String.format("$%,d", precio)
                .replace(",", ".");
    }
}