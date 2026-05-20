package com.university.project.hotelmanagement.controller;

import com.university.project.hotelmanagement.dto.AIChatRequest;
import com.university.project.hotelmanagement.dto.AIChatResponse;
import com.university.project.hotelmanagement.services.OllamaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
public class AIController {


    private final OllamaService ollamaService;

    public AIController(OllamaService ollamaService) {
        this.ollamaService = ollamaService;
    }

    @PostMapping("/chat")
    public ResponseEntity<AIChatResponse> chat(@Valid @RequestBody AIChatRequest request) {
        String aiResponse = ollamaService.chat(request.prompt());
        return ResponseEntity.ok(new AIChatResponse(aiResponse));
    }
}
