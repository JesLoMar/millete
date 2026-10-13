export interface SavingsGoal {
  readonly id: string;
  readonly userId: string;
  readonly name: string;
  readonly targetAmount: number;
  readonly currentAmount: number;
  readonly deadline?: string;
  readonly priority: 'LOW' | 'MEDIUM' | 'HIGH';
  readonly link?: string;
  readonly active: boolean;
}

export interface CreateSavingsGoalDTO {
  name: string;
  targetAmount: number;
  deadline?: string;
  priority: 'LOW' | 'MEDIUM' | 'HIGH';
  link?: string;
}

export interface UpdateSavingsGoalDTO {
  name: string;
  targetAmount: number;
  deadline?: string;
  priority: 'LOW' | 'MEDIUM' | 'HIGH';
  link?: string;
}