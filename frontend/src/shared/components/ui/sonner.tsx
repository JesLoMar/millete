import { Toaster as SonnerToaster } from 'sonner';

export const Toaster = () => {
  return (
    <>
      {/* Desktop: abajo derecha */}
      <div className="hidden sm:block">
        <SonnerToaster
          position="bottom-right"
          visibleToasts={4}
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
      </div>
      {/* Móvil: arriba centrado */}
      <div className="sm:hidden">
        <SonnerToaster
          position="top-center"
          visibleToasts={3}
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
      </div>
    </>
  );
};