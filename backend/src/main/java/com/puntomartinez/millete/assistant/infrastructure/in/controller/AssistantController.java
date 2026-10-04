package com.millete.assistant.infrastructure.in.controller;

import com.millete.assistant.domain.ports.in.InterpretUserInputCommand;
import com.millete.assistant.domain.ports.in.InterpretUserInputUseCase;
import com.millete.assistant.infrastructure.in.controller.dto.InterpretationResponseDTO;
import com.millete.assistant.infrastructure.in.controller.dto.InterpretUserInputRequestDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/assistant")
public class AssistantController {

    private final InterpretUserInputUseCase interpretUserInputUseCase;

    public AssistantController(
            InterpretUserInputUseCase interpretUserInputUseCase
    ) {
        this.interpretUserInputUseCase = interpretUserInputUseCase;
    }

    @PostMapping("/interpret")
    public ResponseEntity<InterpretationResponseDTO> interpret(
            @Valid @RequestBody InterpretUserInputRequestDTO request
    ) {
        var command = new InterpretUserInputCommand(request.input());

        var result = interpretUserInputUseCase.interpret(command);

        return ResponseEntity.ok(
                InterpretationResponseDTO.from(result)
        );
    }
}