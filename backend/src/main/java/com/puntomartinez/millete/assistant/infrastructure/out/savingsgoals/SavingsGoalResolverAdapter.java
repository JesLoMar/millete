package com.puntomartinez.millete.assistant.infrastructure.out.savingsgoals;

import com.puntomartinez.millete.assistant.domain.model.interpretation.SavingsGoalCandidate;
import com.puntomartinez.millete.assistant.domain.model.interpretation.SavingsGoalResolution;
import com.puntomartinez.millete.assistant.domain.ports.out.SavingsGoalResolver;
import com.puntomartinez.millete.savingsgoals.domain.model.SavingsGoal;
import com.puntomartinez.millete.savingsgoals.domain.ports.out.SavingsGoalRepository;
import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class SavingsGoalResolverAdapter
        implements SavingsGoalResolver {

    private final SavingsGoalRepository savingsGoalRepository;

    public SavingsGoalResolverAdapter(
            SavingsGoalRepository savingsGoalRepository
    ) {
        this.savingsGoalRepository = savingsGoalRepository;
    }

    @Override
    public SavingsGoalResolution resolve(
            UUID userId,
            String goalReference
    ) {
        if (goalReference == null
                || goalReference.isBlank()) {

            throw new IllegalArgumentException(
                    "goalReference cannot be null or blank"
            );
        }

        String normalizedReference =
                normalize(goalReference);

        List<SavingsGoal> goals =
                savingsGoalRepository.findAllByUserId(userId);

        List<SavingsGoal> exactMatches =
                goals.stream()
                        .filter(goal ->
                                normalize(goal.getName())
                                        .equals(normalizedReference)
                        )
                        .toList();

        if (exactMatches.size() == 1) {
            return SavingsGoalResolution.found(
                    goalReference,
                    toCandidate(exactMatches.getFirst())
            );
        }

        if (exactMatches.size() > 1) {
            return SavingsGoalResolution.ambiguous(
                    goalReference
            );
        }

        /*
         * Fallback sencillo para expresiones naturales:
         *
         * "coche" -> "Coche nuevo"
         * "vacaciones" -> "Vacaciones Japón"
         *
         * Solo aceptamos el resultado si existe un único candidato.
         */
        List<SavingsGoal> partialMatches =
                new ArrayList<>();

        for (SavingsGoal goal : goals) {
            String normalizedGoalName =
                    normalize(goal.getName());

            if (normalizedGoalName.contains(normalizedReference)
                    || normalizedReference.contains(normalizedGoalName)) {

                partialMatches.add(goal);
            }
        }

        if (partialMatches.size() == 1) {
            return SavingsGoalResolution.found(
                    goalReference,
                    toCandidate(partialMatches.getFirst())
            );
        }

        if (partialMatches.size() > 1) {
            return SavingsGoalResolution.ambiguous(
                    goalReference
            );
        }

        return SavingsGoalResolution.notFound(
                goalReference
        );
    }

    private SavingsGoalCandidate toCandidate(
            SavingsGoal goal
    ) {
        return new SavingsGoalCandidate(
                goal.getId(),
                goal.getName()
        );
    }

    private String normalize(String value) {
        String normalized =
                Normalizer.normalize(
                        value,
                        Normalizer.Form.NFD
                );

        return normalized
                .replaceAll("\\p{M}", "")
                .toLowerCase()
                .trim()
                .replaceAll("\\s+", " ");
    }
}