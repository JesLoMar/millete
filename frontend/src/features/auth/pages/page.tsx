import { AuthForm } from '@/features/auth/components/AuthForm';
import { InfoSection } from '@/features/auth/components/InfoSection';
import { BackgroundDecoration } from '@/features/auth/components/BackgroundDecoration';
import { useAuth } from '../context/AuthContext';
import { Navigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';

export const LoginPage = () => {
  const { t } = useTranslation()
  const { isAuthenticated, isLoading } = useAuth();

  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-background">
        <div className="animate-pulse text-muted-foreground">{t("common.loading")}</div>
      </div>
    );
  }

  if (isAuthenticated) {
    return <Navigate to="/dashboard" replace />;
  }

  return (
    <main 
      className="min-h-screen flex bg-background selection:bg-primary/30 selection:text-white"
      aria-label="Página de inicio de sesión"
    >
      <section 
        className="w-full lg:w-1/3 shrink-0 relative z-10"
        aria-label="Formulario de autenticación"
      >
        <AuthForm />
      </section>

      <section 
        className="hidden lg:block lg:w-2/3"
        aria-label="Información y novedades"
      >
        <InfoSection />
      </section>

      <BackgroundDecoration />
    </main>
  );
};