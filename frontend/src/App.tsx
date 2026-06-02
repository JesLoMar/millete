import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { AuthProvider } from '@/features/auth/context/AuthContext';
import { LoginPage } from '@/features/auth/pages/page';
import { ProtectedRoute } from '@/app/router/ProtectedRoute';
import { PublicRoute } from '@/app/router/PublicRoute';
import { DashboardPage } from './features/dashboard/pages/page';
import { TransactionsPage } from '@/features/transactions/pages/page';
import { CategoriesPage } from '@/features/categories/pages/page';
import { InvestmentsPage } from '@/features/investments/pages/page';
import { FamilyPage } from '@/features/family/pages/page';
import { JoinFamilyPage } from '@/features/family/pages/JoinFamilyPage';
import { Toaster } from '@/shared/components/ui/sonner';
import WikiLayout from '@/features/wiki/components/WikiLayout';
import WikiPage from '@/features/wiki/pages/page';

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

          {/* === WIKI (pública, sin autenticación) === */}
          <Route path="/wiki" element={<WikiLayout />}>
            <Route index element={<WikiPage />} />
            <Route path=":section" element={<WikiPage />} />
          </Route>

          {/* === RUTAS PRIVADAS === */}
          <Route element={<ProtectedRoute />}>
            <Route path="/join-family" element={<JoinFamilyPage />} />
            <Route path="/dashboard" element={<DashboardPage />} />
            <Route path="/transactions" element={<TransactionsPage />} />
            <Route path="/categories" element={<CategoriesPage />} />
            <Route path="/investments" element={<InvestmentsPage />} />
            <Route path="/family" element={<FamilyPage />} />
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