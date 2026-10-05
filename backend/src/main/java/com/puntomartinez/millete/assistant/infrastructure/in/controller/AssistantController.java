package com.puntomartinez.millete.assistant.infrastructure.in.controller;

import com.puntomartinez.millete.assistant.domain.ports.in.InterpretUserInputCommand;
import com.puntomartinez.millete.assistant.domain.ports.in.InterpretUserInputUseCase;
import com.puntomartinez.millete.assistant.infrastructure.in.controller.dto.InterpretationResponseDTO;
import com.puntomartinez.millete.assistant.infrastructure.in.controller.dto.InterpretUserInputRequestDTO;
import com.puntomartinez.millete.shared.infrastructure.in.controller.dto.JwtUser;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/assistant")
public class AssistantController {

    private final InterpretUserInputUseCase interpretUserInputUseCase;

    public AssistantController(
            InterpretUserInputUseCase interpretUserInputUseCase
    ) {
        this.interpretUserInputUseCase =
                interpretUserInputUseCase;
    }

    @PostMapping("/interpret")
    public ResponseEntity<InterpretationResponseDTO> interpret(
            @Valid @RequestBody InterpretUserInputRequestDTO request,
            Authentication authentication
    ) {
        UUID userId =
                ((JwtUser) authentication.getPrincipal()).getId();

        var command =
                new InterpretUserInputCommand(
                        userId,
                        request.input()
                );

        var result =
                interpretUserInputUseCase.interpret(command);

        return ResponseEntity.ok(
                InterpretationResponseDTO.from(result)
        );
    }
}