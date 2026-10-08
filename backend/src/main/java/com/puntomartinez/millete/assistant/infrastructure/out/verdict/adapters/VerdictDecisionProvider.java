package com.puntomartinez.millete.assistant.infrastructure.out.verdict.adapters;

import com.puntomartinez.millete.assistant.domain.model.AppAction;
import com.puntomartinez.millete.assistant.domain.model.Confidence;
import com.puntomartinez.millete.assistant.domain.model.interpretation.AiInterpretation;
import com.puntomartinez.millete.assistant.domain.ports.out.AiDecisionProvider;
import com.puntomartinez.millete.assistant.infrastructure.out.verdict.client.VerdictClient;
import com.puntomartinez.millete.assistant.infrastructure.out.verdict.config.VerdictProperties;
import com.puntomartinez.millete.assistant.infrastructure.out.verdict.dto.VerdictDecisionRequestDTO;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class VerdictDecisionProvider implements AiDecisionProvider {

private static final String QUESTION_PROMPT = """
        Identify the Millete application action that best matches the
        user's intention.

        The user may write in any of the languages supported by Millete.
        Interpret the meaning and intention semantically, regardless of
        the language used. Do not require literal word matching.

        Choose the action that best represents what the user wants to do.

        IMPORTANT DISAMBIGUATION RULES:

        SAVINGS GOALS:

        - ADD_SAVING_GOAL means creating a NEW savings goal that does not
          exist yet.

          Examples:
          "Quiero ahorrar 5000 euros para un coche."
          "Quiero crear un objetivo para las vacaciones."
          "I want to save 5000 euros for a car."

          The user is defining a new savings goal.

        - EDIT_SAVING_GOAL means modifying an EXISTING savings goal.

          Expressions such as "mi objetivo", "el objetivo", "my goal",
          or equivalent expressions in another language usually refer
          to an existing goal when the user is changing one of its
          properties.

          Examples:
          "Pon mi objetivo coche en 5000 euros."
          "Cambia mi objetivo vacaciones a 3000 euros."
          "Modifica la fecha de mi objetivo coche."
          "Change my car goal to 5000 euros."

          Important:
          If the user changes the TARGET AMOUNT of an existing goal,
          the action is EDIT_SAVING_GOAL.

        - ADD_SAVING_GOAL_CONTRIBUTION means adding MONEY to an EXISTING
          savings goal.

          Expressions such as "añade", "aporta", "ingresa", "mete",
          "deposita", "contribuye", or equivalent expressions in another
          language indicate a contribution when the user is adding a
          specific amount to an existing savings goal.

          Examples:
          "Añade 200 euros a mi objetivo coche."
          "Aporta 500 euros a mi objetivo vacaciones."
          "Ingresa 100 euros en mi objetivo coche."
          "Add 200 euros to my car savings goal."

          Important:
          If the user adds money to the amount ALREADY SAVED in an
          existing goal, the action is ADD_SAVING_GOAL_CONTRIBUTION.

        CRITICAL DISTINCTION:

        "Quiero ahorrar 5000 euros para un coche."
        -> ADD_SAVING_GOAL

        "Pon mi objetivo coche en 5000 euros."
        -> EDIT_SAVING_GOAL

        "Añade 500 euros a mi objetivo coche."
        -> ADD_SAVING_GOAL_CONTRIBUTION

        The presence of a monetary amount alone does NOT determine the
        action. Consider whether the user is:

        1. Creating a new savings goal.
        2. Changing a property of an existing savings goal.
        3. Adding money to the current saved amount of an existing
           savings goal.

        Do not confuse changing the target amount with adding a
        contribution.

        The same semantic distinctions apply regardless of the language
        used by the user.
        """;

private static final List<VerdictDecisionRequestDTO.OptionDTO> OPTIONS =
        Arrays.stream(AppAction.values())
                .map(action ->
                        new VerdictDecisionRequestDTO.OptionDTO(
                                action.name(),
                                descriptionFor(action)
                        )
                )
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
public AiInterpretation decide(String input) {

    var request =
            new VerdictDecisionRequestDTO(
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

    var response =
            verdictClient.decide(request);

    if (response == null
            || response.answers() == null
            || response.answers().isEmpty()) {

        return AiInterpretation.unknown(
                new Confidence(
                        0.0,
                        0.0,
                        true
                )
        );
    }

    var answer =
            response.answers().getFirst();

    var confidence =
            new Confidence(
                    answer.confidence().probability(),
                    answer.confidence().margin(),
                    answer.confidence().abstain()
            );

    if (confidence.abstain()) {
        return AiInterpretation.unknown(
                confidence
        );
    }

    var action =
            parseAction(
                    answer.label()
            );

    if (action == null) {
        return AiInterpretation.unknown(
                confidence
        );
    }

    return AiInterpretation.of(
            action,
            confidence,
            null
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
                """
                Create a new expense category.
                The user wants to create a category that does not exist yet.
                This is not about modifying an existing category.
                """;

        case EDIT_CATEGORY ->
                """
                Modify an existing expense category.
                The user wants to change data of a category that already exists,
                such as its name, description or budget.
                """;

        case ADD_EXPENSE_TRANSACTION ->
                """
                Record a new one-time expense transaction.
                The user wants to register money spent in a single transaction.
                This is not a recurring expense.
                """;

        case ADD_INCOME_TRANSACTION ->
                """
                Record a new one-time income transaction.
                The user wants to register money received in a single transaction.
                This is not a recurring income.
                """;

        case EDIT_TRANSACTION ->
                """
                Modify an existing one-time transaction.
                The user wants to change data of a transaction that already exists,
                such as its description, amount, category or type.
                This is not about a recurring transaction.
                """;

        case ADD_RECURRING_EXPENSE_TRANSACTION ->
                """
                Create a new recurring expense.
                The user wants to register an expense that repeats automatically
                over time, such as daily, weekly, monthly or yearly.
                This is not a one-time expense and not an existing recurring
                transaction being edited.
                """;

        case ADD_RECURRING_INCOME_TRANSACTION ->
                """
                Create a new recurring income.
                The user wants to register income that repeats automatically
                over time, such as daily, weekly, monthly or yearly.
                This is not a one-time income and not an existing recurring
                transaction being edited.
                """;

        case EDIT_RECURRING_TRANSACTION ->
                """
                Modify an existing recurring transaction.
                The user wants to change data of a recurring income or expense
                that already exists, such as its amount, description, type or
                recurrence settings.
                """;
        
        case EDIT_SAVING_GOAL ->
                """
                EDIT an EXISTING savings goal.
                Use this action when the user wants to change a property
                of a goal that already exists.

                This includes changing:
                - the target amount
                - the name
                - the priority
                - the deadline
                - the link

                Typical intent:
                "Pon mi objetivo coche en 5000 euros."
                "Cambia mi objetivo vacaciones a 3000 euros."
                "Modifica la fecha de mi objetivo coche."
                "Change my car goal to 5000 euros."

                IMPORTANT:
                Changing the target amount of an existing goal is
                EDIT_SAVING_GOAL.

                This is NOT creating a new savings goal.
                This is NOT adding money to the amount already saved.
                """;

        case ADD_SAVING_GOAL_CONTRIBUTION ->
                """
                ADD MONEY to an EXISTING savings goal.
                Use this action when the user wants to increase the amount
                of money ALREADY SAVED in a goal.

                Strong indicators are verbs meaning:
                add money, contribute, deposit, put money into, transfer money to,
                make a contribution.

                Examples:
                "Añade 500 euros a mi objetivo coche."
                "Aporta 500 euros a mi objetivo vacaciones."
                "Ingresa 500 euros en mi objetivo coche."
                "Mete 500 euros en mi objetivo coche."
                "Add 500 euros to my car goal."
                "Contribute 500 euros to my car goal."

                IMPORTANT:
                The amount mentioned here is money being ADDED to the
                CURRENT SAVED AMOUNT of the existing goal.

                This is NOT:
                - creating a new goal
                - changing the target amount of the goal

                If the user says "pon el objetivo en 5000 euros", that is
                EDIT_SAVING_GOAL.

                If the user says "añade 500 euros al objetivo", that is
                ADD_SAVING_GOAL_CONTRIBUTION.
                """;

        case ADD_SAVING_GOAL ->
                """
                CREATE a brand-new savings goal.
                Use this action only when the user wants to create or define
                a goal that does not already exist.

                Typical intent:
                "Quiero ahorrar 5000 euros para un coche."
                "Quiero crear un objetivo para las vacaciones."
                "I want to save 5000 euros for a car."

                The user is defining a new savings goal.

                Do NOT use this action when the user refers to an existing
                goal and wants to change it or add money to it.
                """;
    };
}
}