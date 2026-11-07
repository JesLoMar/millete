import { useTranslation } from 'react-i18next';

import { AddCategoryDialog } from '@/features/categories/components/AddCategoryDialog';
import { ContributionModal } from '@/features/savingsgoals/components/ContributionModal';
import { SavingsGoalDialog } from '@/features/savingsgoals/components/SavingsGoalDialog';
import { SavingsGoalEditDialog } from '@/features/savingsgoals/components/SavingsGoalEditDialog';
import { useAddContribution } from '@/features/savingsgoals/hooks/useSavingsGoals';
import type { SavingsGoal } from '@/features/savingsgoals/types';
import { EditRecurringTransactionDialog } from '@/features/transactions/components/dialogs/EditRecurringTransactionDialog';
import { EditTransactionDialog } from '@/features/transactions/components/dialogs/EditTransactionDialog';
import { NewRecurringTransactionDialog } from '@/features/transactions/components/dialogs/NewRecurringTransactionDialog';
import { NewTransactionDialog } from '@/features/transactions/components/dialogs/NewTransactionDialog';
import type { Transaction } from '@/features/transactions/components/types';
import type { PlannedTransaction } from '@/features/transactions/hooks/usePlannedTransactions';

import type {
  AddCategoryData,
  AddRecurringTransactionData,
  AddSavingsGoalContributionData,
  AddSavingsGoalData,
  AddTransactionData,
  AssistantInterpretation,
  EditRecurringTransactionData,
  EditSavingsGoalData,
  EditTransactionData,
  FieldChange,
} from '../types';

import { AssistantNotice } from './AssistantNotice';

interface AssistantActionDialogProps {
  interpretation: AssistantInterpretation;
  onClose: () => void;
}

function getToday(): string {
  return new Date().toISOString().split('T')[0];
}

function resolveValue<T>(
  change: FieldChange<T>,
  fallback: T | null,
  empty: T,
): T {
  if (change.specified) {
    return change.value ?? empty;
  }

  return fallback ?? empty;
}

function resolveOptional<T>(
  change: FieldChange<T>,
  fallback: T | null,
): T | undefined {
  if (change.specified) {
    return change.value ?? undefined;
  }

  return fallback ?? undefined;
}

function toNumberString(
  value: number | null,
): string {
  return value !== null ? String(value) : '';
}

export function AssistantActionDialog({
  interpretation,
  onClose,
}: AssistantActionDialogProps) {
  const { t } = useTranslation('dashboard');
  const { mutate: addContribution, isPending: isAddingContribution } =
    useAddContribution();

  const notice = (
    <AssistantNotice interpretation={interpretation} />
  );

  const { action, data } = interpretation;

  if (!action || !data) {
    return null;
  }

  const handleOpenChange = (open: boolean) => {
    if (!open) {
      onClose();
    }
  };

  const unsupported = (
    <p className="text-sm text-destructive">
      {t('assistant.errors.unsupported')}
    </p>
  );

  switch (action) {
    case 'ADD_EXPENSE_TRANSACTION':
    case 'ADD_INCOME_TRANSACTION': {
      const transaction = data as AddTransactionData;

      return (
        <NewTransactionDialog
          open={true}
          onOpenChange={handleOpenChange}
          notice={notice}
          initialValues={{
            description: transaction.description ?? '',
            category: transaction.categoryId ?? '',
            amount: toNumberString(transaction.amount),
            type:
              action === 'ADD_INCOME_TRANSACTION'
                ? 'INCOME'
                : 'EXPENSE',
          }}
        />
      );
    }

    case 'ADD_RECURRING_EXPENSE_TRANSACTION':
    case 'ADD_RECURRING_INCOME_TRANSACTION': {
      const recurring =
        data as AddRecurringTransactionData;

      return (
        <NewRecurringTransactionDialog
          open={true}
          onOpenChange={handleOpenChange}
          notice={notice}
          initialValues={{
            description: recurring.description ?? '',
            category: recurring.categoryId ?? '',
            amount: toNumberString(recurring.amount),
            type:
              action === 'ADD_RECURRING_INCOME_TRANSACTION'
                ? 'INCOME'
                : 'EXPENSE',
            frequencyType: recurring.frequencyType ?? '',
            frequencyInterval: String(
              recurring.frequencyInterval ?? 1,
            ),
            startDate: recurring.startDate ?? '',
            endDate: recurring.endDate ?? '',
          }}
        />
      );
    }

    case 'ADD_CATEGORY': {
      const category = data as AddCategoryData;

      return (
        <AddCategoryDialog
          open={true}
          onOpenChange={handleOpenChange}
          notice={notice}
          initialValues={{
            name: category.name ?? '',
            description: category.description ?? '',
            budgetLimit: toNumberString(
              category.budgetLimit,
            ),
          }}
        />
      );
    }

    case 'ADD_SAVING_GOAL': {
      const goal = data as AddSavingsGoalData;

      return (
        <SavingsGoalDialog
          open={true}
          onOpenChange={handleOpenChange}
          notice={notice}
          initialValues={{
            name: goal.name ?? '',
            targetAmount: goal.targetAmount ?? undefined,
            priority: goal.priority ?? 'MEDIUM',
            deadline: goal.deadline ?? undefined,
            link: goal.link ?? undefined,
          }}
        />
      );
    }

    case 'ADD_SAVING_GOAL_CONTRIBUTION': {
      const contribution =
        data as AddSavingsGoalContributionData;

      const goal: SavingsGoal = {
        id: contribution.target.id ?? '',
        userId: '',
        name: contribution.target.name ?? '',
        targetAmount:
          contribution.target.targetAmount ?? 0,
        currentAmount:
          contribution.target.currentAmount ?? 0,
        priority: 'MEDIUM',
        active: true,
      };

      return (
        <ContributionModal
          isOpen={true}
          onClose={onClose}
          goal={goal}
          isSubmitting={isAddingContribution}
          initialAmount={
            contribution.amount ?? undefined
          }
          notice={notice}
          onSubmit={(amount) => {
            if (!goal.id) {
              return;
            }

            addContribution(
              { id: goal.id, amount },
              { onSuccess: onClose },
            );
          }}
        />
      );
    }

    case 'EDIT_TRANSACTION': {
      const edit = data as EditTransactionData;
      const { target, changes } = edit;

      const categoryId = resolveValue(
        changes.categoryId,
        target.categoryId,
        '',
      );

      const categoryName = resolveValue(
        changes.categoryName,
        target.categoryName,
        '',
      );

      const transaction: Transaction = {
        id: target.id ?? '',
        description: resolveValue(
          changes.description,
          target.description,
          '',
        ),
        category: categoryName,
        categoryColor: null,
        categoryId,
        amount: resolveValue(
          changes.amount,
          target.amount,
          0,
        ),
        type: resolveValue(
          changes.type,
          target.type,
          'EXPENSE',
        ),
        date: target.date ?? getToday(),
        active: true,
      };

      return (
        <EditTransactionDialog
          open={true}
          onOpenChange={handleOpenChange}
          transaction={transaction}
          notice={notice}
        />
      );
    }

    case 'EDIT_RECURRING_TRANSACTION': {
      const edit =
        data as EditRecurringTransactionData;
      const { target, changes } = edit;

      const transaction: PlannedTransaction = {
        id: target.id ?? '',
        description: resolveValue(
          changes.description,
          target.description,
          '',
        ),
        categoryName: target.categoryName ?? '',
        categoryId: target.categoryId ?? null,
        amount: resolveValue(
          changes.amount,
          target.amount,
          0,
        ),
        type: resolveValue(
          changes.type,
          target.type,
          'EXPENSE',
        ),
        frequencyType: resolveValue(
          changes.frequencyType,
          target.frequencyType,
          'MONTHS',
        ),
        frequencyInterval: resolveValue(
          changes.frequencyInterval,
          target.frequencyInterval,
          1,
        ),
        startDate: target.startDate ?? getToday(),
        endDate: target.endDate ?? null,
        lastExecutedDate: null,
        active: true,
      };

      return (
        <EditRecurringTransactionDialog
          open={true}
          onOpenChange={handleOpenChange}
          transaction={transaction}
          notice={notice}
        />
      );
    }

    case 'EDIT_SAVING_GOAL': {
      const edit = data as EditSavingsGoalData;
      const { target, changes } = edit;

      const goal: SavingsGoal = {
        id: target.id ?? '',
        userId: '',
        name: resolveValue(
          changes.name,
          target.name,
          '',
        ),
        targetAmount: resolveValue(
          changes.targetAmount,
          target.targetAmount,
          0,
        ),
        currentAmount: target.currentAmount ?? 0,
        deadline: resolveOptional(
          changes.deadline,
          target.deadline,
        ),
        priority: resolveValue(
          changes.priority,
          target.priority,
          'MEDIUM',
        ),
        link: resolveOptional(
          changes.link,
          target.link,
        ),
        active: true,
      };

      return (
        <SavingsGoalEditDialog
          open={true}
          onOpenChange={handleOpenChange}
          goal={goal}
          notice={notice}
        />
      );
    }

    default:
      return unsupported;
  }
}
