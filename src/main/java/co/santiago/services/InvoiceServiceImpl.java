package co.santiago.services;

import co.santiago.dto.*;
import co.santiago.models.Invoice;
import co.santiago.models.Item;
import co.santiago.models.LineItem;
import co.santiago.repositories.InvoiceRepository;
import co.santiago.repositories.ItemRepositories;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class InvoiceServiceImpl implements InvoiceService {

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private ItemRepositories itemRepository;

    @Override
    @Transactional
    public InvoiceDTO createInvoice(CreateInvoiceDTO createInvoiceDTO) {

        Invoice invoice = new Invoice();
        invoice.setFecha(LocalDateTime.now());

        int total = 0;

        for (InvoiceItemRequestDTO requestedItem : createInvoiceDTO.getItems()) {

            Item item = itemRepository.findById(requestedItem.getItemId())
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Item no encontrado con id: "
                                            + requestedItem.getItemId()
                            )
                    );

            LineItem lineItem = new LineItem();

            lineItem.setItemId(item.getId());
            lineItem.setNombre(item.getNombre());
            lineItem.setPrecio(item.getPrecio());
            lineItem.setCantidad(requestedItem.getCantidad());

            invoice.addLineItem(lineItem);

            total += item.getPrecio() * requestedItem.getCantidad();
        }

        invoice.setTotal(total);

        Invoice savedInvoice = invoiceRepository.save(invoice);

        return convertToDTO(savedInvoice);
    }

    @Override
    public InvoiceDTO getInvoiceById(Long id) {

        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Factura no encontrada con id: " + id
                        )
                );

        return convertToDTO(invoice);
    }

    private InvoiceDTO convertToDTO(Invoice invoice) {

        InvoiceDTO invoiceDTO = new InvoiceDTO();

        invoiceDTO.setId(invoice.getId());
        invoiceDTO.setFecha(invoice.getFecha());
        invoiceDTO.setTotal(invoice.getTotal());
        invoiceDTO.setEstado(invoice.getEstado());

        List<InvoiceItemDTO> itemsDTO = invoice.getLineItems()
                .stream()
                .map(lineItem -> {

                    InvoiceItemDTO itemDTO = new InvoiceItemDTO();

                    itemDTO.setId(lineItem.getItemId());
                    itemDTO.setNombre(lineItem.getNombre());
                    itemDTO.setPrecio(lineItem.getPrecio());
                    itemDTO.setCantidad(lineItem.getCantidad());

                    return itemDTO;
                })
                .toList();

        invoiceDTO.setItems(itemsDTO);

        return invoiceDTO;
    }
    @Override
    @Transactional
    public InvoiceDTO updateEstado(Long id, String estado) {

        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Factura no encontrada con id: " + id
                        )
                );

        invoice.setEstado(estado);

        Invoice updatedInvoice = invoiceRepository.save(invoice);

        return convertToDTO(updatedInvoice);
    }
}