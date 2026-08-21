package co.santiago.services;

import co.santiago.dto.ChatResponseDTO;

public interface DataQueryService {

    ChatResponseDTO ask(String question);
}