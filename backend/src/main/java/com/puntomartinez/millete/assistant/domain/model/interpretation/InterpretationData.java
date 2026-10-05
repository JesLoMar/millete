package com.puntomartinez.millete.assistant.domain.model.interpretation;

public sealed interface InterpretationData
        permits AddCategoryData,
                EditCategoryData,
                AddTransactionData,
                EditTransactionData,
                AddRecurringTransactionData,
                EditRecurringTransactionData,
                AddSavingsGoalData,
                EditSavingsGoalData,
                AddSavingsGoalContributionData {
}