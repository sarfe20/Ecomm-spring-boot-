package com.ecommerce.project.controller;

import com.ecommerce.project.payload.ChatRequestDTO;
import com.ecommerce.project.payload.ChatResponseDTO;
import com.ecommerce.project.service.ChatbotService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/chatbot")
public class ChatbotController {
    private final ChatbotService chatbotService;

    public ChatbotController(ChatbotService chatbotService) {
        this.chatbotService = chatbotService;
    }

    @PostMapping
    public ResponseEntity<ChatResponseDTO> chat(@Valid @RequestBody ChatRequestDTO chatRequestDTO,
                                                Authentication authentication) {
        return new ResponseEntity<>(chatbotService.chat(chatRequestDTO, authentication), HttpStatus.OK);
    }
}
