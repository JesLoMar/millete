import { useTranslation } from "react-i18next"
import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { useAuth } from '@/features/auth/context/AuthContext'

export const ProtectedRoute = () => {
  const { t } = useTranslation()
  const { isAuthenticated, isLoading } = useAuth()
  const location = useLocation()

  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <p className="text-muted-foreground">{t("common.loadingSession")}</p>
      </div>
    )
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" state={{ from: location }} replace />
  }

  return <Outlet />
}