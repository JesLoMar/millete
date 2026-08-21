export const ROUTES = {
  home: '/',
  login: '/login',
  dashboard: '/dashboard',
  transactions: '/transactions',
  categories: '/categories',
  investments: '/investments',
  groupGoals: '/group-goals',
  joinGroupGoal: '/join-group-goal',
  profile: '/profile',
  savingsGoals: '/savings-goals',
  notifications: '/notifications',
  wiki: '/wiki',
} as const;

export const PROTECTED_ROUTE_PATHS: readonly string[] = [
  ROUTES.dashboard,
  ROUTES.transactions,
  ROUTES.categories,
  ROUTES.investments,
  ROUTES.groupGoals,
  ROUTES.joinGroupGoal,
  ROUTES.profile,
  ROUTES.savingsGoals,
  ROUTES.notifications,
];