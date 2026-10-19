import { ROUTES } from './routes';

const prefetched = new Set<string>();

const IMPORTERS: Record<string, () => Promise<unknown>> = {
  [ROUTES.dashboard]: () =>
    import('@/features/dashboard/pages/page'),
  [ROUTES.transactions]: () =>
    import('@/features/transactions/pages/page'),
  [ROUTES.categories]: () =>
    import('@/features/categories/pages/page'),
  [ROUTES.investments]: () =>
    import('@/features/investments/pages/page'),
  [ROUTES.groupGoals]: () =>
    import('@/features/groupgoals/pages/page'),
  [ROUTES.profile]: () =>
    import('@/features/profile/pages/page'),
  [ROUTES.savingsGoals]: () =>
    import('@/features/savingsgoals/pages/page'),
  [ROUTES.notifications]: () =>
    import('@/features/notifications/pages/page'),
};

export function prefetchRoute(path: string): void {
  if (prefetched.has(path)) return;
  const importer = IMPORTERS[path];
  if (!importer) return;
  prefetched.add(path);
  importer().catch(() => {

    prefetched.delete(path);
  });
}