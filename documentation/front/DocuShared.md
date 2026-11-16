# Infraestructura compartida

## API y sesión

[`axiosClient.ts`](../../frontend/src/shared/api/axiosClient.ts) centraliza Axios, usa `VITE_API_URL` o su fallback de desarrollo, `withCredentials` y timeout de 30 s. No agrega Bearer token. Los errores 401 pueden disparar `auth:logout`; `skipAuthErrorHandler` y `skipGlobalErrorNotify` ajustan estos manejadores por solicitud.

[`sessionCache.ts`](../../frontend/src/shared/utils/sessionCache.ts) usa `sessionStorage` solo para datos visibles no sensibles (usuario e ID de sesión). La sesión real depende de cookie HTTP-only; el caché no ofrece seguridad contra XSS.

## Paginación

[`useServerPagination.ts`](../../frontend/src/shared/hooks/useServerPagination.ts) combina bloques consultados al servidor con páginas de visualización más pequeñas. Los tamaños dependen de cada consumidor. Conserva datos previos durante transiciones, puede pedir el bloque contiguo cuando una página cruza el límite y precarga bloques siguientes; no asumir un tamaño universal.

## Navegación y componentes

`shared/config/navigation.ts` alimenta la navegación habilitada, pero no define las rutas montadas. `shared/components` contiene navegación, selectores, encabezado, tarjetas de métricas y componentes visuales reutilizables; `shared/components/core` ofrece primitivas UI.

## Formato y notificaciones

`shared/utils/i18nFormat.ts` y `date.ts` formatean datos usando configuración de locale y moneda local. [`notify.tsx`](../../frontend/src/shared/utils/notifications/notify.tsx) expone toast; la presentación se apoya en Sonner.

## Temas

`shared/themes/palettes.ts`, `applyTheme.ts` y `hooks/useTheme.ts` implementan paletas y aplicación de variables CSS. Consultar esas fuentes para valores y persistencia vigentes.

## Manejo HTTP

`axiosClient` toma `VITE_API_URL` o el fallback `/api/v1` de desarrollo, timeout de 30 segundos, JSON y credenciales incluidas. El interceptor de request registra el auth epoch, no adjunta token. En response, cancelaciones se propagan; 401 puede emitir evento global salvo petición auth, petición marcada `skipAuthErrorHandler` o epoch antiguo. Otros errores generan toast salvo `skipGlobalErrorNotify`, con texto de respuesta `message`, `error`, mensaje Axios o traducción; códigos 403, 404, 409, 5xx y timeout tienen descripción contextual.

## Paginador compartido

El hook recibe `queryKey`, `fetchPage`, `serverSize`, `displaySize` y `enabled`. `fetchPage` retorna `{content,currentPage,totalPages,totalElements,size,first,last}`. El hook transforma página visual a bloque servidor, combina dos bloques si una página cruza el límite, conserva últimos elementos durante la transición y precarga el bloque siguiente en proximidad. Rechaza tamaños no enteros/positivos o `displaySize > serverSize`. stale time interno: 30 segundos.

## Piezas de navegación

`navigation.ts` registra elementos habilitados/deshabilitados; `Sidebar` consume ese registro y `TopNav` compone encabezado, selección de idioma/tema y cuenta. Componentes centrales concretos incluyen `Header`, `PeriodSelector`, `Pagination`, `MetricCard` y los controles de `shared/components/core`. No inferir existencia de ruta solo porque aparezca en navegación.
