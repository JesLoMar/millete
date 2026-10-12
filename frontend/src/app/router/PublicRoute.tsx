import {
  Navigate,
  Outlet,
  useLocation,
  useSearchParams,
} from 'react-router-dom';

import { ROUTES } from './routes';
import { useAuth } from '@/features/auth/context/AuthContext';
import { sanitizeRedirect } from '@/features/auth/utils/sanitizeRedirect';

export const PublicRoute = () => {
  const { isAuthenticated } = useAuth();
  const location = useLocation();
  const [searchParams] = useSearchParams();

  if (!isAuthenticated) {
    return <Outlet />;
  }

  const from = location.state?.from;

  const fromState = from?.pathname
    ? `${from.pathname}${from.search || ''}${from.hash || ''}`
    : null;

  const destination =
    sanitizeRedirect(fromState) ??
    sanitizeRedirect(searchParams.get('redirect')) ??
    ROUTES.dashboard;

  return <Navigate to={destination} replace />;
};