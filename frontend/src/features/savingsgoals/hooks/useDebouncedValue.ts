import { useEffect, useState } from 'react';

/**
 * Devuelve `value` con retraso: solo se actualiza cuando deja de cambiar
 * durante `delay` ms. Si ya tienes un hook equivalente en shared/, úsalo.
 */
export function useDebouncedValue<T>(value: T, delay = 300): T {
  const [debounced, setDebounced] = useState(value);

  useEffect(() => {
    const id = window.setTimeout(() => setDebounced(value), delay);

    return () => window.clearTimeout(id);
  }, [value, delay]);

  return debounced;
}
