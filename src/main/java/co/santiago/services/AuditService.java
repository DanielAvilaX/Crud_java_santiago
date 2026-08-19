package co.santiago.services;

public interface AuditService {

    void log(
            String entidad,
            Long entidadId,
            String accion,
            String usuario,
            Object valorAnterior,
            Object valorNuevo
    );
}