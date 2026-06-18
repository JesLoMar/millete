export interface SavingsGoal {
  id: string;
  userId: string;
  name: string;
  targetAmount: number;
  currentAmount: number;
  deadline?: string;
  priority: 'LOW' | 'MEDIUM' | 'HIGH';
  status: 'ACTIVE' | 'PAUSED' | 'COMPLETED' | 'CANCELLED';
  link?: string;
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
  status: 'ACTIVE' | 'PAUSED' | 'COMPLETED' | 'CANCELLED';
  link?: string;
}
