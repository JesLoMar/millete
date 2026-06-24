import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { AuthProvider } from '@/features/auth/context/AuthContext';
import { LoginPage } from '@/features/auth/pages/page';
import { ProtectedRoute } from '@/app/router/ProtectedRoute';
import { PublicRoute } from '@/app/router/PublicRoute';
import { DashboardPage } from './features/dashboard/pages/page';
import { TransactionsPage } from '@/features/transactions/pages/page';
import { CategoriesPage } from '@/features/categories/pages/page';
import { InvestmentsPage } from '@/features/investments/pages/page';
import { GroupGoalsPage } from '@/features/groupgoals/pages/page';
import { JoinGroupGoalPage } from '@/features/groupgoals/pages/JoinGroupGoalPage';
import { Toaster } from '@/shared/components/core/sonner';
import WikiLayout from '@/features/wiki/components/WikiLayout';
import WikiPage from '@/features/wiki/pages/page';
import { ProfilePage } from '@/features/profile/pages/page';
import { SavingsGoalsPage } from '@/features/savingsgoals/pages/page';

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          {/* === RUTAS PÚBLICAS === */}
          <Route element={<PublicRoute />}>
            <Route path="/" element={<LoginPage />} />
            <Route path="/login" element={<LoginPage />} />
          </Route>

          {/* === WIKI === */}
          <Route path="/wiki" element={<WikiLayout />}>
            <Route index element={<WikiPage />} />
            <Route path=":section" element={<WikiPage />} />
          </Route>

          {/* === RUTAS PRIVADAS === */}
          <Route element={<ProtectedRoute />}>
            <Route path="/join-group-goal" element={<JoinGroupGoalPage />} />
            <Route path="/dashboard" element={<DashboardPage />} />
            <Route path="/transactions" element={<TransactionsPage />} />
            <Route path="/categories" element={<CategoriesPage />} />
            <Route path="/investments" element={<InvestmentsPage />} />
            <Route path="/group-goals" element={<GroupGoalsPage />} />
            <Route path="/profile" element={<ProfilePage />} />
            <Route path="/savings-goals" element={<SavingsGoalsPage />} />
          </Route>

          {/* Ruta 404 - Redirige a dashboard si autenticado, sino a login */}
          <Route path="*" element={<ProtectedRoute />}>
            <Route path="*" element={<DashboardPage />} />
          </Route>
        </Routes>
      </BrowserRouter>
      <Toaster />
    </AuthProvider>
  );
}
