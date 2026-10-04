package com.puntomartinez.millete.assistant.infrastructure.out.verdict.dto;

import java.util.List;

public record VerdictDecisionRequestDTO(
        String decider,
        List<DecisionDTO> decisions
) {

    public record DecisionDTO(
            String input,
            QuestionDTO question
    ) {
    }

    public record QuestionDTO(
            String kind,
            String prompt,
            List<OptionDTO> options
    ) {
    }

    public record OptionDTO(
            String label,
            String description
    ) {
    }
}