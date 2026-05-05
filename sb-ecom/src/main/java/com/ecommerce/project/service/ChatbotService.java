package com.ecommerce.project.service;

import com.ecommerce.project.payload.ChatRequestDTO;
import com.ecommerce.project.payload.ChatResponseDTO;
import org.springframework.security.core.Authentication;

public interface ChatbotService {
    ChatResponseDTO chat(ChatRequestDTO chatRequestDTO, Authentication authentication);
}
