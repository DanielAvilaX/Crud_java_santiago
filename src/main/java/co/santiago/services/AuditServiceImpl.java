package co.santiago.services;

import co.santiago.models.AuditLog;
import co.santiago.repositories.AuditLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuditServiceImpl implements AuditService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void log(
            String entidad,
            Long entidadId,
            String accion,
            String usuario,
            Object valorAnterior,
            Object valorNuevo
    ) {
        try {

            AuditLog auditLog = new AuditLog();

            auditLog.setEntidad(entidad);
            auditLog.setEntidadId(entidadId);
            auditLog.setAccion(accion);
            auditLog.setUsuario(usuario);
            auditLog.setFecha(LocalDateTime.now());

            if (valorAnterior != null) {
                auditLog.setValorAnterior(
                        objectMapper.writeValueAsString(valorAnterior)
                );
            }

            if (valorNuevo != null) {
                auditLog.setValorNuevo(
                        objectMapper.writeValueAsString(valorNuevo)
                );
            }

            auditLogRepository.save(auditLog);

        } catch (Exception e) {
            throw new RuntimeException(
                    "Error guardando auditoría: " + e.getMessage(),
                    e
            );
        }
    }
}