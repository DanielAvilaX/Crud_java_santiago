package co.santiago.services;

import co.santiago.dto.ItemsDTO;
import co.santiago.models.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import co.santiago.repositories.ItemRepositories;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ItemServiceImpl implements ItemService {
    @Autowired
    private ItemRepositories itemRepository;
    @Autowired
    private  S3bucketService itemsS3bucketService;
    @Autowired
    @Qualifier("itemsCopy")
    private S3bucketService itemsCopyS3bucketService;

    @Override
    @Transactional
    public ItemsDTO saveItem(ItemsDTO itemsDTO) {

        Item item = new Item();
        item.setNombre(itemsDTO.getNombre());
        item.setPrecio(itemsDTO.getPrecio());

        Item savedItem = itemRepository.saveAndFlush(item);

        itemsDTO.setId(savedItem.getId());

        itemsS3bucketService.saveItem(itemsDTO);
        itemsCopyS3bucketService.saveItem(itemsDTO);

        return itemsDTO;
    }
    @Override
    public Page<ItemsDTO> getAllItems(int page, int size) {

        Pageable pageable = PageRequest.of(page, size);

        Page<Item> items = itemRepository.findAll(pageable);

        return items.map(item -> {
            ItemsDTO dto = new ItemsDTO();
            dto.setId(item.getId());
            dto.setNombre(item.getNombre());
            dto.setPrecio(item.getPrecio());
            return dto;
        });
    }
    @Override
    @Transactional
    public ItemsDTO updateItem(Long id, ItemsDTO itemsDTO) {

        Item item = itemRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Item no encontrado con id: " + id)
                );

        item.setNombre(itemsDTO.getNombre());
        item.setPrecio(itemsDTO.getPrecio());

        Item updatedItem = itemRepository.saveAndFlush(item);

        itemsDTO.setId(updatedItem.getId());

        itemsS3bucketService.saveItem(itemsDTO);
        itemsCopyS3bucketService.saveItem(itemsDTO);

        return itemsDTO;
    }
}