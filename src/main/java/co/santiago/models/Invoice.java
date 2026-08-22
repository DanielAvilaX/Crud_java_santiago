package co.santiago.models;

import co.santiago.enums.InvoiceStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@NoArgsConstructor
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime fecha;

    @Enumerated(EnumType.STRING)
    private InvoiceStatus estado = InvoiceStatus.PENDIENTE_DE_PAGO;

    private Integer total;

    @Version
    private Long version;

    @OneToMany(
            mappedBy = "invoice",
            cascade = CascadeType.ALL
    )
    private List<Facturados> facturados = new ArrayList<>();

    public void addFacturado(Facturados facturados) {
        this.facturados.add(facturados);
        facturados.setInvoice(this);
    }
}