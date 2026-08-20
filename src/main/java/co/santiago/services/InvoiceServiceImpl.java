package co.santiago.services;

import co.santiago.dto.*;
import co.santiago.enums.AuditAction;
import co.santiago.enums.InvoiceStatus;
import co.santiago.exceptions.InvoiceAlreadyPaidException;
import co.santiago.exceptions.InvoiceNotFoundException;
import co.santiago.exceptions.ItemInactiveException;
import co.santiago.exceptions.ItemNotFoundException;
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

    @Autowired
    private AuditService auditService;

    @Override
    @Transactional
    public InvoiceDTO createInvoice(CreateInvoiceDTO createInvoiceDTO) {

        Invoice invoice = new Invoice();
        invoice.setFecha(LocalDateTime.now());

        int total = 0;

        for (InvoiceItemRequestDTO requestedItem : createInvoiceDTO.getItems()) {

            Item item = itemRepository.findById(requestedItem.getItemId())
                    .orElseThrow(() ->
                            new ItemNotFoundException(
                                    requestedItem.getItemId()
                            )
                    );

            if (item.isDeleted()) {
                throw new ItemInactiveException(item.getId());
            }

            LineItem lineItem = new LineItem();

            lineItem.setItemId(item.getId());
            lineItem.setNombre(item.getNombre());
            lineItem.setDescripcion(item.getDescripcion());
            lineItem.setPrecio(item.getPrecio());
            lineItem.setCantidad(requestedItem.getCantidad());

            invoice.addLineItem(lineItem);

            total += item.getPrecio() * requestedItem.getCantidad();
        }

        invoice.setTotal(total);

        Invoice savedInvoice = invoiceRepository.save(invoice);

        auditService.log(
                "Invoice",
                savedInvoice.getId(),
                AuditAction.CREATE,
                "santiago",
                null,
                savedInvoice
        );

        return convertToDTO(savedInvoice);
    }

    @Override
    public InvoiceDTO getInvoiceById(Long id) {

        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() ->
                        new InvoiceNotFoundException(id)
                );

        return convertToDTO(invoice);
    }

    @Override
    @Transactional
    public InvoiceDTO addItem(
            Long invoiceId,
            InvoiceItemRequestDTO itemRequest
    ) {

        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() ->
                        new InvoiceNotFoundException(invoiceId)
                );

        if (invoice.getEstado() != InvoiceStatus.PENDIENTE_DE_PAGO) {
            throw new InvoiceAlreadyPaidException(invoice.getId());
        }

        Invoice before = copyInvoice(invoice);

        Item item = itemRepository.findById(itemRequest.getItemId())
                .orElseThrow(() ->
                        new ItemNotFoundException(
                                itemRequest.getItemId()
                        )
                );

        if (item.isDeleted()) {
            throw new ItemInactiveException(item.getId());
        }

        LineItem existingLineItem = invoice.getLineItems()
                .stream()
                .filter(lineItem ->
                        lineItem.getItemId().equals(item.getId())
                )
                .findFirst()
                .orElse(null);

        if (existingLineItem != null) {

            existingLineItem.setCantidad(
                    existingLineItem.getCantidad()
                            + itemRequest.getCantidad()
            );

        } else {

            LineItem lineItem = new LineItem();

            lineItem.setItemId(item.getId());
            lineItem.setNombre(item.getNombre());
            lineItem.setDescripcion(item.getDescripcion());
            lineItem.setPrecio(item.getPrecio());
            lineItem.setCantidad(itemRequest.getCantidad());

            invoice.addLineItem(lineItem);
        }

        int total = invoice.getLineItems()
                .stream()
                .mapToInt(lineItem ->
                        lineItem.getPrecio()
                                * lineItem.getCantidad()
                )
                .sum();

        invoice.setTotal(total);

        Invoice updatedInvoice = invoiceRepository.save(invoice);

        auditService.log(
                "Invoice",
                updatedInvoice.getId(),
                AuditAction.UPDATE,
                "santiago",
                before,
                updatedInvoice
        );

        return convertToDTO(updatedInvoice);
    }

    private InvoiceDTO convertToDTO(Invoice invoice) {

        InvoiceDTO invoiceDTO = new InvoiceDTO();

        invoiceDTO.setId(invoice.getId());
        invoiceDTO.setFecha(invoice.getFecha());
        invoiceDTO.setEstado(invoice.getEstado());

        invoiceDTO.setTotalFormateado(
                formatPrecio(invoice.getTotal())
        );

        List<InvoiceItemDTO> itemsDTO = invoice.getLineItems()
                .stream()
                .map(lineItem -> {

                    InvoiceItemDTO itemDTO = new InvoiceItemDTO();

                    itemDTO.setId(lineItem.getItemId());
                    itemDTO.setNombre(lineItem.getNombre());
                    itemDTO.setDescripcion(lineItem.getDescripcion());
                    itemDTO.setCantidad(lineItem.getCantidad());

                    itemDTO.setPrecioFormateado(
                            formatPrecio(lineItem.getPrecio())
                    );

                    return itemDTO;
                })
                .toList();

        invoiceDTO.setItems(itemsDTO);

        int totalProductos = invoice.getLineItems()
                .stream()
                .mapToInt(LineItem::getCantidad)
                .sum();

        invoiceDTO.setTotalProductos(totalProductos);

        return invoiceDTO;
    }

    private Invoice copyInvoice(Invoice invoice) {

        Invoice copy = new Invoice();

        copy.setId(invoice.getId());
        copy.setFecha(invoice.getFecha());
        copy.setTotal(invoice.getTotal());
        copy.setEstado(invoice.getEstado());

        for (LineItem lineItem : invoice.getLineItems()) {

            LineItem lineCopy = new LineItem();

            lineCopy.setId(lineItem.getId());
            lineCopy.setItemId(lineItem.getItemId());
            lineCopy.setNombre(lineItem.getNombre());
            lineCopy.setDescripcion(lineItem.getDescripcion());
            lineCopy.setPrecio(lineItem.getPrecio());
            lineCopy.setCantidad(lineItem.getCantidad());

            copy.addLineItem(lineCopy);
        }

        return copy;
    }

    private String formatPrecio(Integer precio) {
        return String.format("$%,d", precio)
                .replace(",", ".");
    }
}