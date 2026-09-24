export interface SavingsGoal {
  readonly id: string;
  readonly userId: string;
  readonly name: string;
  readonly targetAmount: number;
  readonly currentAmount: number;
  readonly deadline?: string;
  readonly priority: 'LOW' | 'MEDIUM' | 'HIGH';
  readonly status:
    | 'ACTIVE'
    | 'PAUSED'
    | 'COMPLETED'
    | 'CANCELLED';
  readonly link?: string;
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
  status:
    | 'ACTIVE'
    | 'PAUSED'
    | 'COMPLETED'
    | 'CANCELLED';
  link?: string;
}