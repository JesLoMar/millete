import { PROTECTED_ROUTE_PATHS } from '@/shared/config/routes';

export function sanitizeRedirect(
  raw: string | null | undefined,
): string | null {
  if (
    !raw ||
    !raw.startsWith('/') ||
    raw.startsWith('//') ||
    raw.includes('\\')
  ) {
    return null;
  }

  try {
    const target = new URL(raw, window.location.origin);

    if (target.origin !== window.location.origin) {
      return null;
    }

    const isAllowed = PROTECTED_ROUTE_PATHS.some(
      (route) =>
        target.pathname === route ||
        target.pathname.startsWith(`${route}/`),
    );

    if (!isAllowed) {
      return null;
    }

    return `${target.pathname}${target.search}${target.hash}`;
  } catch {
    return null;
  }
}