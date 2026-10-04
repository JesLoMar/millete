package com.millete.assistant.infrastructure.out.verdict.dto;

import java.util.List;
import java.util.Map;

public record VerdictDecisionResponseDTO(
        List<AnswerDTO> answers
) {

    public record AnswerDTO(
            String label,
            ConfidenceDTO confidence,
            Map<String, Double> distribution,
            List<String> ranked,
            String engine,
            Double latency_ms
    ) {
    }

    public record ConfidenceDTO(
            double probability,
            double margin,
            double entropy,
            boolean abstain,
            boolean calibrated,
            Object conformal_set
    ) {
    }
}