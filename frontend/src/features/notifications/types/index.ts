export type NotificationType = 'GOAL_INVITATION' | 'SYSTEM';

export interface Notification {
  id: string;
  type: NotificationType;
  title: string;
  message: string;
  metadata: Record<string, string>;
  read: boolean;
  actionRequired: boolean;
  actionedAt: string | null;
  createdAt: string;
  expiresAt: string | null;
}

export interface NotificationCountResponse {
  count: number;
}
