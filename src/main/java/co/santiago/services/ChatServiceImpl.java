package co.santiago.services;

import co.santiago.dto.ChatRequestDTO;
import co.santiago.dto.ChatResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ChatServiceImpl implements ChatService {

    private static final int MAX_ATTEMPTS = 3;

    private final DataQueryService dataQueryService;

    public ChatServiceImpl(
            DataQueryService dataQueryService
    ) {
        this.dataQueryService = dataQueryService;
    }

    @Override
    public ChatResponseDTO ask(ChatRequestDTO request) {

        Exception lastException = null;

        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {

            try {

                log.info(
                        "[chat:attempt] intento {}/{} pregunta='{}'",
                        attempt,
                        MAX_ATTEMPTS,
                        request.getPregunta()
                );

                ChatResponseDTO response =
                        dataQueryService.ask(
                                request.getPregunta()
                        );

                log.info(
                        "[chat:success] respuesta obtenida en intento {}",
                        attempt
                );

                return response;

            } catch (Exception e) {

                lastException = e;

                log.warn(
                        "[chat:retry] intento {}/{} falló: {}",
                        attempt,
                        MAX_ATTEMPTS,
                        e.getMessage()
                );

                if (attempt < MAX_ATTEMPTS) {

                    try {
                        Thread.sleep(1000L * attempt);
                    } catch (InterruptedException interruptedException) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException(
                                "Retry interrumpido",
                                interruptedException
                        );
                    }
                }
            }
        }

        log.error(
                "[chat:error] todos los intentos fallaron",
                lastException
        );

        throw new RuntimeException(
                "No fue posible procesar la pregunta después de "
                        + MAX_ATTEMPTS
                        + " intentos",
                lastException
        );
    }
}