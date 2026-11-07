import type { TransactionType } from '@/features/transactions';

export type AssistantStatus =
  | 'READY'
  | 'NEEDS_INFORMATION'
  | 'UNKNOWN';

export type AssistantAction =
  | 'ADD_CATEGORY'
  | 'EDIT_CATEGORY'
  | 'ADD_EXPENSE_TRANSACTION'
  | 'ADD_INCOME_TRANSACTION'
  | 'EDIT_TRANSACTION'
  | 'ADD_RECURRING_EXPENSE_TRANSACTION'
  | 'ADD_RECURRING_INCOME_TRANSACTION'
  | 'EDIT_RECURRING_TRANSACTION'
  | 'ADD_SAVING_GOAL'
  | 'EDIT_SAVING_GOAL'
  | 'ADD_SAVING_GOAL_CONTRIBUTION';

export type AssistantFrequencyType =
  | 'DAYS'
  | 'WEEKS'
  | 'MONTHS'
  | 'YEARS';

export type AssistantPriority = 'LOW' | 'MEDIUM' | 'HIGH';

export interface FieldChange<T> {
  specified: boolean;
  value: T | null;
}

export interface MissingField {
  field: string;
}

export type UnresolvedEntityType =
  | 'CATEGORY'
  | 'TRANSACTION'
  | 'RECURRING_TRANSACTION'
  | 'SAVINGS_GOAL';

export type UnresolvedEntityStatus =
  | 'NOT_FOUND'
  | 'AMBIGUOUS';

export interface UnresolvedEntity {
  type: UnresolvedEntityType;
  reference: string;
  status: UnresolvedEntityStatus;
}

export interface AppliedDefault {
  field: string;
  reason: string;
}

export interface AddCategoryData {
  name: string | null;
  description: string | null;
  budgetLimit: number | null;
}

export interface AddTransactionData {
  description: string | null;
  amount: number | null;
  categoryName: string | null;
  categoryId: string | null;
}

export interface AddRecurringTransactionData {
  description: string | null;
  amount: number | null;
  categoryName: string | null;
  categoryId: string | null;
  frequencyType: AssistantFrequencyType | null;
  frequencyInterval: number | null;
  startDate: string | null;
  endDate: string | null;
}

export interface AddSavingsGoalData {
  name: string | null;
  targetAmount: number | null;
  priority: AssistantPriority | null;
  deadline: string | null;
  link: string | null;
}

export interface SavingsGoalTarget {
  id: string | null;
  name: string | null;
  targetAmount: number | null;
  currentAmount: number | null;
}

export interface AddSavingsGoalContributionData {
  target: SavingsGoalTarget;
  amount: number | null;
}

export interface TransactionTarget {
  id: string | null;
  description: string | null;
  amount: number | null;
  categoryName: string | null;
  categoryId: string | null;
  date: string | null;
  type: TransactionType | null;
}

export interface TransactionChanges {
  description: FieldChange<string>;
  amount: FieldChange<number>;
  categoryName: FieldChange<string>;
  categoryId: FieldChange<string>;
  type: FieldChange<TransactionType>;
}

export interface EditTransactionData {
  target: TransactionTarget;
  changes: TransactionChanges;
}

export interface RecurringTransactionTarget {
  id: string | null;
  description: string | null;
  amount: number | null;
  categoryName: string | null;
  categoryId: string | null;
  frequencyType: AssistantFrequencyType | null;
  frequencyInterval: number | null;
  type: TransactionType | null;
  startDate: string | null;
  endDate: string | null;
}

export interface RecurringTransactionChanges {
  description: FieldChange<string>;
  amount: FieldChange<number>;
  type: FieldChange<TransactionType>;
  frequencyType: FieldChange<AssistantFrequencyType>;
  frequencyInterval: FieldChange<number>;
}

export interface EditRecurringTransactionData {
  target: RecurringTransactionTarget;
  changes: RecurringTransactionChanges;
}

export interface EditSavingsGoalTarget {
  id: string | null;
  name: string | null;
  targetAmount: number | null;
  currentAmount: number | null;
  priority: AssistantPriority | null;
  deadline: string | null;
  link: string | null;
}

export interface EditSavingsGoalChanges {
  name: FieldChange<string>;
  targetAmount: FieldChange<number>;
  priority: FieldChange<AssistantPriority>;
  deadline: FieldChange<string>;
  link: FieldChange<string>;
}

export interface EditSavingsGoalData {
  target: EditSavingsGoalTarget;
  changes: EditSavingsGoalChanges;
}

export type AssistantData =
  | AddCategoryData
  | AddTransactionData
  | AddRecurringTransactionData
  | AddSavingsGoalData
  | AddSavingsGoalContributionData
  | EditTransactionData
  | EditRecurringTransactionData
  | EditSavingsGoalData;

export interface AssistantInterpretation {
  status: AssistantStatus;
  action: AssistantAction | null;
  probability: number;
  margin: number;
  abstain: boolean;
  data: AssistantData | null;
  missingFields: MissingField[];
  unresolvedEntities: UnresolvedEntity[];
  defaultsApplied: AppliedDefault[];
}
