package co.santiago.services;

import co.santiago.dto.ItemsDTO;
import org.springframework.data.domain.Page;

public interface ItemService {
    ItemsDTO saveItem(ItemsDTO itemsDTO);
    Page<ItemsDTO> getAllItems(int page, int size);
    ItemsDTO updateItem(Long id, ItemsDTO itemsDTO);
}
