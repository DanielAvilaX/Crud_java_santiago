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
import co.santiago.models.Facturados;
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

            Facturados facturados = new Facturados();

            facturados.setItemId(item.getId());
            facturados.setNombre(item.getNombre());
            facturados.setDescripcion(item.getDescripcion());
            facturados.setPrecio(item.getPrecio());
            facturados.setCantidad(requestedItem.getCantidad());

            invoice.addLineItem(facturados);

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

        Facturados existingFacturados = invoice.getFacturados()
                .stream()
                .filter(facturados ->
                        facturados.getItemId().equals(item.getId())
                )
                .findFirst()
                .orElse(null);

        if (existingFacturados != null) {

            existingFacturados.setCantidad(
                    existingFacturados.getCantidad()
                            + itemRequest.getCantidad()
            );

        } else {

            Facturados facturados = new Facturados();

            facturados.setItemId(item.getId());
            facturados.setNombre(item.getNombre());
            facturados.setDescripcion(item.getDescripcion());
            facturados.setPrecio(item.getPrecio());
            facturados.setCantidad(itemRequest.getCantidad());

            invoice.addLineItem(facturados);
        }

        int total = invoice.getFacturados()
                .stream()
                .mapToInt(facturados ->
                        facturados.getPrecio()
                                * facturados.getCantidad()
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

        invoiceDTO.setTotal(
                formatPrecio(invoice.getTotal())
        );

        List<InvoiceItemDTO> itemsDTO = invoice.getFacturados()
                .stream()
                .map(facturados -> {

                    InvoiceItemDTO itemDTO = new InvoiceItemDTO();

                    itemDTO.setId(facturados.getItemId());
                    itemDTO.setNombre(facturados.getNombre());
                    itemDTO.setDescripcion(facturados.getDescripcion());

                    itemDTO.setPrecioUnidad(
                            formatPrecio(facturados.getPrecio())
                    );

                    itemDTO.setCantidad(
                            facturados.getCantidad()
                    );

                    int totalProducto =
                            facturados.getPrecio()
                                    * facturados.getCantidad();

                    itemDTO.setTotalProducto(
                            formatPrecio(totalProducto)
                    );

                    return itemDTO;
                })
                .toList();

        invoiceDTO.setItems(itemsDTO);

        int totalProductos = invoice.getFacturados()
                .stream()
                .mapToInt(Facturados::getCantidad)
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

        for (Facturados facturados : invoice.getFacturados()) {

            Facturados lineCopy = new Facturados();

            lineCopy.setId(facturados.getId());
            lineCopy.setItemId(facturados.getItemId());
            lineCopy.setNombre(facturados.getNombre());
            lineCopy.setDescripcion(facturados.getDescripcion());
            lineCopy.setPrecio(facturados.getPrecio());
            lineCopy.setCantidad(facturados.getCantidad());

            copy.addLineItem(lineCopy);
        }

        return copy;
    }

    private String formatPrecio(Integer precio) {
        return String.format("$%,d", precio)
                .replace(",", ".");
    }
}