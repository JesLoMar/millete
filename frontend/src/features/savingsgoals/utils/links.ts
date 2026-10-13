export const normalizeHttpLink = (
  link?: string,
): string | undefined => {
  const trimmed = link?.trim();

  if (!trimmed) {
    return undefined;
  }

  const hasProtocol = /^[a-z][a-z\d+.-]*:\/\//i.test(trimmed);

  const withProtocol = hasProtocol
    ? trimmed
    : `https://${trimmed}`;

  try {
    const url = new URL(withProtocol);

    if (url.protocol !== 'http:' && url.protocol !== 'https:') {
      return undefined;
    }

    // Rechaza "mailto:a@b" (que se interpretaría como usuario@host) y
    // cualquier URL con credenciales incrustadas.
    if (url.username || url.password) {
      return undefined;
    }

    // Rechaza hosts sin punto ("foo") salvo localhost.
    if (url.hostname !== 'localhost' && !url.hostname.includes('.')) {
      return undefined;
    }

    return url.href;
  } catch {
    return undefined;
  }
};

export const isValidHttpLink = (link: string): boolean => {
  const trimmed = link.trim();

  if (!trimmed) {
    return true;
  }

  return normalizeHttpLink(trimmed) !== undefined;
};