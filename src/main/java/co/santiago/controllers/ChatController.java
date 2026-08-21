package co.santiago.controllers;

import co.santiago.dto.ChatRequestDTO;
import co.santiago.dto.ChatResponseDTO;
import co.santiago.services.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @Operation(
            summary = "Consultar asistente del negocio"
    )
    @PostMapping
    public ResponseEntity<ChatResponseDTO> chat(
            @Valid @RequestBody ChatRequestDTO request
    ) {

        return ResponseEntity.ok(
                chatService.ask(request)
        );
    }
}
