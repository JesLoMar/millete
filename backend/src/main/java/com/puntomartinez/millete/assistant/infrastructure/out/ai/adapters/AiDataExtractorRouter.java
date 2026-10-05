package com.puntomartinez.millete.assistant.infrastructure.out.ai.adapters;

import com.puntomartinez.millete.assistant.domain.model.AppAction;
import com.puntomartinez.millete.assistant.domain.model.interpretation.AiExtractionContext;
import com.puntomartinez.millete.assistant.domain.model.interpretation.InterpretationData;
import com.puntomartinez.millete.assistant.domain.ports.out.AiDataExtractor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

@Component("aiDataExtractorRouter")
public final class AiDataExtractorRouter
        implements AiDataExtractor {

    private final Map<
            AppAction,
            AiDataExtractor
            > extractors;

    public AiDataExtractorRouter(
            AddCategoryDataExtractor addCategoryDataExtractor,
            AddTransactionDataExtractor addTransactionDataExtractor
    ) {
        this.extractors =
                Map.of(
                        AppAction.ADD_CATEGORY,
                        Objects.requireNonNull(
                                addCategoryDataExtractor
                        ),

                        AppAction.ADD_EXPENSE_TRANSACTION,
                        Objects.requireNonNull(
                                addTransactionDataExtractor
                        )
                );
    }

    @Override
    public InterpretationData extract(
            AiExtractionContext context
    ) {
        Objects.requireNonNull(
                context,
                "context cannot be null"
        );

        var extractor =
                extractors.get(
                        context.action()
                );

        if (extractor == null) {
            throw new IllegalArgumentException(
                    "No AI extractor configured for action: "
                            + context.action()
            );
        }

        return extractor.extract(context);
    }
}