package co.santiago.services;

import co.santiago.dto.InvoiceDTO;
import co.santiago.dto.ItemRequestDTO;
import co.santiago.dto.ItemsDTO;
import co.santiago.models.Item;
import org.springframework.data.domain.Page;

public interface ItemService {

    ItemsDTO saveItem(ItemRequestDTO itemRequestDTO);

    Page<ItemsDTO> getAllItems(int page, int size);

    ItemsDTO updateItem(Long id, ItemRequestDTO itemRequestDTO);

    Item getItemsById(Long id);


    void deleteItem(Long id);
}