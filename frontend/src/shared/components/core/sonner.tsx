import { useEffect, useState } from 'react';
import { Toaster as SonnerToaster } from 'sonner';

// Una sola instancia de Toaster; la posición se adapta al viewport
// con matchMedia en lugar de montar dos instancias ocultas por CSS.
function useIsDesktop(): boolean {
  const [isDesktop, setIsDesktop] = useState(
    () => window.matchMedia('(min-width: 640px)').matches
  );
  useEffect(() => {
    const mq = window.matchMedia('(min-width: 640px)');
    const onChange = (e: MediaQueryListEvent) => setIsDesktop(e.matches);
    mq.addEventListener('change', onChange);
    return () => mq.removeEventListener('change', onChange);
  }, []);
  return isDesktop;
}

export const Toaster = () => {
  const isDesktop = useIsDesktop();
  return (
    <SonnerToaster
      position={isDesktop ? 'bottom-right' : 'top-center'}
      visibleToasts={isDesktop ? 4 : 3}
      closeButton={false}
      richColors={false}
      gap={8}
      offset={16}
      toastOptions={{
        classNames: {
          toast: 'group flex items-start w-full max-w-sm font-sans text-sm',
          closeButton: 'hidden',
        },
      }}
    />
  );
};
