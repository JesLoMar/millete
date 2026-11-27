package com.puntomartinez.millete.assistant.infrastructure.in.controller;

import com.puntomartinez.millete.assistant.domain.ports.in.AssistantAvailabilityUseCase;
import com.puntomartinez.millete.assistant.domain.ports.in.InterpretUserInputCommand;
import com.puntomartinez.millete.assistant.domain.ports.in.InterpretUserInputUseCase;
import com.puntomartinez.millete.assistant.infrastructure.in.controller.dto.InterpretationResponseDTO;
import com.puntomartinez.millete.assistant.infrastructure.in.controller.dto.InterpretUserInputRequestDTO;
import com.puntomartinez.millete.shared.infrastructure.in.controller.dto.JwtUser;
import jakarta.validation.Valid;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/assistant")
public class AssistantController {

    private final InterpretUserInputUseCase interpretUserInputUseCase;
    private final AssistantAvailabilityUseCase assistantAvailabilityUseCase;

    public AssistantController(
            InterpretUserInputUseCase interpretUserInputUseCase,
            AssistantAvailabilityUseCase assistantAvailabilityUseCase
    ) {
        this.interpretUserInputUseCase =
                interpretUserInputUseCase;
        this.assistantAvailabilityUseCase = assistantAvailabilityUseCase;
    }

    @GetMapping("/availability")
    public ResponseEntity<AvailabilityResponse> availability() {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(new AvailabilityResponse(assistantAvailabilityUseCase.isAvailable()));
    }

    public record AvailabilityResponse(boolean available) {}

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
