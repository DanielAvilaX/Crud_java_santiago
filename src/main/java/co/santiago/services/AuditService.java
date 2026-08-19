package co.santiago.services;

import co.santiago.enums.AuditAction;

public interface AuditService {

    void log(
            String entidad,
            Long entidadId,
            AuditAction accion,
            String usuario,
            Object valorAnterior,
            Object valorNuevo
    );
}