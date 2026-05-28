import { Toaster as SonnerToaster } from 'sonner';

export const Toaster = () => {
  return (
    <SonnerToaster
      position="bottom-right"
      visibleToasts={4}
      closeButton={false}
      richColors={false}
      toastOptions={{
        classNames: {
          toast: 'group flex items-start w-full max-w-sm font-sans text-sm',
          closeButton: 'hidden',
        },
      }}
    />
  );
};