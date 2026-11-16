# Documentación del frontend

Índice de guías técnicas contrastadas con el código de `frontend/src`. Estas páginas describen el cliente y sus contratos observables; no sustituyen la implementación ni especifican garantías del servidor.

## Guías

| Área | Documento | Código principal |
|---|---|---|
| Arranque y dependencias | [Configuración raíz](DocuMain.md#configuración-raíz) | [`frontend/package.json`](../../frontend/package.json), [`frontend/src/main.tsx`](../../frontend/src/main.tsx), [`frontend/src/App.tsx`](../../frontend/src/App.tsx) |
| Aplicación | [Raíz](DocuRaiz.md) | [`frontend/src/App.tsx`](../../frontend/src/App.tsx) |
| Enrutamiento | [Rutas](DocuRutes.md) | [`frontend/src/App.tsx`](../../frontend/src/App.tsx), [`frontend/src/shared/config/routes.ts`](../../frontend/src/shared/config/routes.ts) |
| Infraestructura compartida | [Shared](DocuShared.md) | [`frontend/src/shared`](../../frontend/src/shared) |
| Internacionalización | [Lib](LibDocu.md) | [`frontend/src/lib/i18n.ts`](../../frontend/src/lib/i18n.ts) |
| Autenticación | [Auth](AuthDocu.md) | [`frontend/src/features/auth`](../../frontend/src/features/auth) |
| Dashboard y exportación | [Dashboard](DashboardDocu.md) | [`frontend/src/features/dashboard`](../../frontend/src/features/dashboard) |
| Categorías | [Categories](CategoriesDocu.md) | [`frontend/src/features/categories`](../../frontend/src/features/categories) |
| Transacciones y recurrencias | [Transactions](TransactionsDocu.md) | [`frontend/src/features/transactions`](../../frontend/src/features/transactions) |
| Inversiones | [Investments](InvestmentsDocu.md) | [`frontend/src/features/investments`](../../frontend/src/features/investments) |
| Metas grupales | [Group Goals](GroupGoalsDocu.md) | [`frontend/src/features/groupgoals`](../../frontend/src/features/groupgoals) |
| Metas personales | [Savings Goals](SavingsGoalsDocu.md) | [`frontend/src/features/savingsgoals`](../../frontend/src/features/savingsgoals) |
| Perfil y preferencias | [Profile](ProfileDocu.md) | [`frontend/src/features/profile`](../../frontend/src/features/profile) |
| Notificaciones | [Notifications](NotificationsDocu.md) | [`frontend/src/features/notifications`](../../frontend/src/features/notifications) |
| Asistente | [Assistant](AssistantDocu.md) | [`frontend/src/features/assistant`](../../frontend/src/features/assistant) |
| Wiki | [Wiki](WikiDocu.md) | No hay página Wiki montada actualmente |
| Ideas móviles | [Estado de propuestas](future.md) | No hay implementación móvil en el frontend |

## Configuración raíz

La aplicación usa React 19, TypeScript, Vite y pnpm. El gestor y comandos válidos se definen en [`frontend/package.json`](../../frontend/package.json). Los comandos de comprobación declarados para pnpm son `pnpm type-check` y `pnpm build`; el script `build:check` invoca `npm run` y no se describe aquí como un flujo pnpm.

`main.tsx` inicializa i18n y React Query, y `App.tsx` compone autenticación, router, rutas lazy y toaster. La configuración del bundler y TypeScript está en [`frontend/vite.config.ts`](../../frontend/vite.config.ts), [`frontend/tsconfig.app.json`](../../frontend/tsconfig.app.json) y [`frontend/tsconfig.node.json`](../../frontend/tsconfig.node.json). Para detalles de rutas y estado de sesión, consulte las guías enlazadas arriba.

Las dependencias de ejecución incluyen React 19, React Router 7, TanStack Query 5, Axios, i18next, React Hook Form, Zod, Tailwind 4, Framer Motion y Sonner. Desarrollo incluye TypeScript 6, Vite 8 y ESLint 10. Los scripts disponibles figuran literalmente en el manifiesto: `dev`, `build`, `type-check`, `build:check`, `lint`, `lint:fix`, `preview`, `analyze` y `doctor`. `build:check` está escrito con `npm run`; usar por separado `pnpm type-check` y `pnpm build` es la secuencia pnpm válida documentada.
