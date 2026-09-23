import { Navigate, Outlet } from 'react-router-dom';

import { ROUTES } from './routes';
import { useAuth } from '@/features/auth/context/AuthContext';

export const PublicRoute = () => {
  const { isAuthenticated } = useAuth();

  if (isAuthenticated) {
    return <Navigate to={ROUTES.dashboard} replace />;
  }

  return <Outlet />;
};