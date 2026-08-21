package co.santiago.services;

import co.santiago.ai.tools.ChatTools;
import co.santiago.dto.ChatRequestDTO;
import co.santiago.dto.ChatResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ChatServiceImpl implements ChatService {

    private final ChatClient chatClient;
    private final ChatTools chatTools;

    public ChatServiceImpl(
            ChatClient.Builder builder,
            ChatTools chatTools
    ) {
        this.chatClient = builder.build();
        this.chatTools = chatTools;
    }

    @Override
    @Cacheable(value = "chatResponses", key = "#request.pregunta")
    public ChatResponseDTO ask(ChatRequestDTO request) {

        log.info(
                "Pregunta recibida: '{}'",
                request.getPregunta()
        );

        // Orquestación automática (Fase 4): el modelo decide por sí mismo
        // qué herramientas llamar y en qué orden (buscar esquema, buscar
        // conocimiento del proyecto, ejecutar SQL, reintentar si falla),
        // en vez de que el código fuerce siempre la misma secuencia fija.
        String respuesta = chatClient
                .prompt()
                .system("""
                        Eres un asistente de negocio para una aplicación de
                        facturación (facturas, pagos, productos).

                        Tienes acceso a herramientas para responder con datos
                        reales, nunca inventados:

                        - searchDatabaseSchema: para saber qué tablas/columnas existen.
                        - searchProjectKnowledge: para saber qué significan los
                          estados y campos del negocio.
                        - executeReadOnlyQuery: para ejecutar un SELECT y obtener datos.

                        REGLAS:

                        - Antes de escribir SQL, usa searchDatabaseSchema y,
                          si la pregunta involucra vocabulario de negocio
                          (estados, métodos de pago, etc.), también
                          searchProjectKnowledge.
                        - Solo ejecutes SELECT. Nunca INSERT, UPDATE, DELETE,
                          DROP, ALTER, CREATE, TRUNCATE o MERGE.
                        - Si executeReadOnlyQuery devuelve un error, corrige
                          la consulta usando el esquema y vuelve a intentar.
                        - No inventes tablas, columnas ni datos.
                        - Si no hay resultados, indícalo claramente.
                        - Expresa los valores monetarios de forma legible.
                        - Responde de forma clara y concisa, en español.
                        """)
                .user(request.getPregunta())
                .tools(chatTools)
                .call()
                .content();

        log.info(
                "Respuesta generada: '{}'",
                respuesta
        );

        return new ChatResponseDTO(respuesta);
    }
}
