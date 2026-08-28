import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { lazy, Suspense } from 'react';
import { AuthProvider } from '@/features/auth/context/AuthContext';
const LoginPage = lazy(() => import('@/features/auth/pages/page').then(m => ({ default: m.LoginPage })));
import { ProtectedRoute } from '@/app/router/ProtectedRoute';
import { PublicRoute } from '@/app/router/PublicRoute';
import { ROUTES } from '@/app/router/routes';
import { Toaster } from '@/shared/components/core/sonner';
import { Spinner } from '@/shared/components/Spinner';
const DashboardPage = lazy(() => import('@/features/dashboard/pages/page').then(m => ({ default: m.DashboardPage })));
const TransactionsPage = lazy(() => import('@/features/transactions/pages/page').then(m => ({ default: m.TransactionsPage })));
const CategoriesPage = lazy(() => import('@/features/categories/pages/page').then(m => ({ default: m.CategoriesPage })));
const InvestmentsPage = lazy(() => import('@/features/investments/pages/page').then(m => ({ default: m.InvestmentsPage })));
const GroupGoalsPage = lazy(() => import('@/features/groupgoals/pages/page').then(m => ({ default: m.GroupGoalsPage })));
const JoinGroupGoalPage = lazy(() => import('@/features/groupgoals/pages/JoinGroupGoalPage').then(m => ({ default: m.JoinGroupGoalPage })));
const WikiLayout = lazy(() => import('@/features/wiki/components/WikiLayout'));
const WikiPage = lazy(() => import('@/features/wiki/pages/page'));
const ProfilePage = lazy(() => import('@/features/profile/pages/page').then(m => ({ default: m.ProfilePage })));
const SavingsGoalsPage = lazy(() => import('@/features/savingsgoals/pages/page').then(m => ({ default: m.SavingsGoalsPage })));
const NotificationsPage = lazy(() => import('@/features/notifications/pages/page'));
function PageLoader() {
  return (
    <div className="flex h-screen w-full items-center justify-center">
      <Spinner size={40} />
    </div>
  );
}
export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Suspense fallback={<PageLoader />}>
          <Routes>
            <Route element={<PublicRoute />}>
              <Route path={ROUTES.home} element={<LoginPage />} />
              <Route path={ROUTES.login} element={<LoginPage />} />
            </Route>
            <Route path={ROUTES.wiki} element={<WikiLayout />}>
              <Route index element={<WikiPage />} />
              <Route path=":section" element={<WikiPage />} />
            </Route>
            <Route element={<ProtectedRoute />}>
              <Route path={ROUTES.joinGroupGoal} element={<JoinGroupGoalPage />} />
              <Route path={ROUTES.dashboard} element={<DashboardPage />} />
              <Route path={ROUTES.transactions} element={<TransactionsPage />} />
              <Route path={ROUTES.categories} element={<CategoriesPage />} />
              <Route path={ROUTES.investments} element={<InvestmentsPage />} />
              <Route path={ROUTES.groupGoals} element={<GroupGoalsPage />} />
              <Route path={ROUTES.profile} element={<ProfilePage />} />
              <Route path={ROUTES.savingsGoals} element={<SavingsGoalsPage />} />
              <Route path={ROUTES.notifications} element={<NotificationsPage />} />
            </Route>
            <Route path="*" element={<ProtectedRoute />}>
              <Route path="*" element={<DashboardPage />} />
            </Route>
          </Routes>
        </Suspense>
      </BrowserRouter>
      <Toaster />
    </AuthProvider>
  );
}