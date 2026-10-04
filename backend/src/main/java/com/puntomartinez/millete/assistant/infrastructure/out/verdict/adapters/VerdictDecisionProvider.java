package com.millete.assistant.infrastructure.out.verdict.adapters;

import com.millete.assistant.domain.model.AppAction;
import com.millete.assistant.domain.model.Confidence;
import com.millete.assistant.domain.model.InterpretationResult;
import com.millete.assistant.domain.ports.out.AiDecisionProvider;
import com.millete.assistant.infrastructure.out.verdict.client.VerdictClient;
import com.millete.assistant.infrastructure.out.verdict.config.VerdictProperties;
import com.millete.assistant.infrastructure.out.verdict.dto.VerdictDecisionRequestDTO;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class VerdictDecisionProvider implements AiDecisionProvider {

    private static final String QUESTION_PROMPT =
            "¿Qué acción de Millete quiere realizar el usuario?";

    private static final List<VerdictDecisionRequestDTO.OptionDTO> OPTIONS =
            Arrays.stream(AppAction.values())
                    .map(action -> new VerdictDecisionRequestDTO.OptionDTO(
                            action.name(),
                            descriptionFor(action)
                    ))
                    .toList();

    private final VerdictClient verdictClient;
    private final VerdictProperties properties;

    public VerdictDecisionProvider(
            VerdictClient verdictClient,
            VerdictProperties properties
    ) {
        this.verdictClient = verdictClient;
        this.properties = properties;
    }

    @Override
    public InterpretationResult decide(String input) {

        var request = new VerdictDecisionRequestDTO(
                properties.decider(),
                List.of(
                        new VerdictDecisionRequestDTO.DecisionDTO(
                                input,
                                new VerdictDecisionRequestDTO.QuestionDTO(
                                        "choose",
                                        QUESTION_PROMPT,
                                        OPTIONS
                                )
                        )
                )
        );

        var response = verdictClient.decide(request);

        if (response == null
                || response.answers() == null
                || response.answers().isEmpty()) {
            return InterpretationResult.unknown(
                    new Confidence(
                            0.0,
                            0.0,
                            true
                    )
            );
        }

        var answer = response.answers().getFirst();

        AppAction action = parseAction(answer.label());

        if (answer.confidence().abstain()) {
            return InterpretationResult.unknown(
                    new Confidence(
                            answer.confidence().probability(),
                            answer.confidence().margin(),
                            true
                    )
            );
        }

        if (action == null) {
            return InterpretationResult.unknown(
                    new Confidence(
                            answer.confidence().probability(),
                            answer.confidence().margin(),
                            false
                    )
            );
        }

        return InterpretationResult.ready(
                action,
                new Confidence(
                        answer.confidence().probability(),
                        answer.confidence().margin(),
                        false
                )
        );
    }

    private static AppAction parseAction(String label) {
        if (label == null) {
            return null;
        }

        try {
            return AppAction.valueOf(label);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private static String descriptionFor(AppAction action) {
        return switch (action) {
            case ADD_CATEGORY ->
                    "Crear una categoría de gasto nueva.";

            case EDIT_CATEGORY ->
                    "Modificar una categoría existente.";

            case ADD_EXPENSE_TRANSACTION ->
                    "Registrar un gasto puntual.";

            case ADD_INCOME_TRANSACTION ->
                    "Registrar un ingreso puntual.";

            case EDIT_TRANSACTION ->
                    "Modificar una transacción existente.";

            case ADD_RECURRING_EXPENSE_TRANSACTION ->
                    "Crear un gasto recurrente.";

            case ADD_RECURRING_INCOME_TRANSACTION ->
                    "Crear un ingreso recurrente.";

            case EDIT_RECURRING_TRANSACTION ->
                    "Modificar un ingreso o gasto recurrente existente.";

            case ADD_SAVING_GOAL ->
                    "Crear un objetivo de ahorro.";

            case EDIT_SAVING_GOAL ->
                    "Modificar un objetivo de ahorro existente.";
        };
    }
}