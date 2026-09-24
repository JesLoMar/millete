export type NotificationType =
  | 'GOAL_INVITATION'
  | 'SYSTEM';

export interface Notification {
  readonly id: string;
  readonly type: NotificationType;
  readonly title: string;
  readonly message: string;
  readonly metadata: Readonly<Record<string, string>>;
  readonly read: boolean;
  readonly actionRequired: boolean;
  readonly actionedAt: string | null;
  readonly createdAt: string;
  readonly expiresAt: string | null;
}

export interface NotificationCountResponse {
  readonly count: number;
}