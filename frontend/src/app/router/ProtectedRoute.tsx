import { useTranslation } from "react-i18next"
import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { useAuth } from '@/features/auth/context/AuthContext'
import { Button } from '@/shared/components/core/button'
import { ROUTES } from './routes'

export const ProtectedRoute = () => {
  const { t } = useTranslation()
  const { isAuthenticated, isLoading, isOffline, retryAuth } = useAuth()
  const location = useLocation()

  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <p className="text-muted-foreground">{t('common:status.loadingSession')}</p>
      </div>
    )
  }

  if (isOffline) {
    return (
      <div className="flex min-h-screen flex-col items-center justify-center gap-4 px-6 text-center">
        <p className="text-muted-foreground">{t('common:status.connectionError')}</p>
        <Button variant="outline" onClick={retryAuth}>
          {t('common:actions.retry')}
        </Button>
      </div>
    )
  }

  if (!isAuthenticated) {
    return <Navigate to={ROUTES.login} state={{ from: location }} replace />
  }

  return <Outlet />
}