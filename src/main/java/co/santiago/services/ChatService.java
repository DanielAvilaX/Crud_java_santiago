package co.santiago.services;

import co.santiago.dto.ChatRequestDTO;
import co.santiago.dto.ChatResponseDTO;

public interface ChatService {

    ChatResponseDTO ask(ChatRequestDTO request);
}