package co.santiago.services;

import co.santiago.dto.ItemsDTO;
import co.santiago.models.Item;
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
}