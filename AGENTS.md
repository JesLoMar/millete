# AGENTS.md

Millete (v0.2.0): personal finance app. Spring Boot 4.0.6 / Java 25 backend, React 19 / Vite 8 frontend, PostgreSQL + Flyway, pnpm only.

## Commands

Frontend (`frontend/`):

- `pnpm install`; `pnpm dev` serves Vite on :5173 and proxies `/api` to `localhost:8080`.
- `pnpm type-check` and `pnpm build` pass on this tree.
- `pnpm lint` currently fails on pre-existing problems (see Baseline failures).
- There is no test runner or `pnpm test`; do not invent one.

Backend (`backend/`):

- Use system `mvn`, not `./mvnw`: `.mvn/wrapper/maven-wrapper.properties` is absent and `.mvn/` is gitignored.
- Build: `mvn "-Dmaven.test.skip=true" package` (PowerShell needs the quotes; otherwise Maven parses `.test.skip=true` as a lifecycle phase).
- `mvn test` currently fails at test compilation (see Baseline failures). `-Dtest=...` does not bypass test compilation.
- Local run expects PostgreSQL at `localhost:5432/millete_db` with `postgres`/`654321` (hardcoded in `application.yml`; Docker overrides via env).

Full stack:

- `./manage.sh <init|start|reload|restart|stop|down|status|logs|restore>` from Git Bash.
- `restart` does not pick up `.env` or config changes; use `reload`.
- `docker compose --profile ai up` also starts `verdict`; it is off by default.

## Layout

Backend is package-by-feature hexagonal under `backend/src/main/java/com/puntomartinez/millete/`:
`assistant, categories, dashboard, dataexport, groupgoals, investments, notifications, plannedtransactions, savingsgoals, transactions, users, shared`.

Each module has `domain/model`, `domain/ports/in|out`, `application/services`, `infrastructure/in/controller`, `infrastructure/out/...`. Cross-module calls go through the other module's domain ports or a dedicated `infrastructure/out` adapter; `shared` is the kernel (exceptions, security, pagination, time).

`frontend/src/features/` mirrors that, with naming mismatches:

- `auth` + `profile` map to backend `users`.
- Recurring transactions live in `transactions` (backend `plannedtransactions`).
- Export/import live in `dashboard`; backend `dataexport` has no own frontend feature.
- Shared code is `src/shared/{api,components,config,hooks,themes,types,utils}`. Route paths and protected routes are defined only in `src/shared/config/routes.ts`.

## Backend rules

- Controllers derive `userId` from `JwtUser` / `Authentication`, never from request params. This is the anti-IDOR rule across every endpoint.
- Auth is a JWT in the HttpOnly `__Host-ms_token` cookie. `JwtAuthenticationFilter` also checks `userSessionRepository.existsByIdAndActiveTrue(sessionId)` on every request, so a valid token for a revoked session must be rejected.
- Do not add `@CrossOrigin` or permissive CORS. CORS, CSRF exemptions and the public-route allowlist are centralized in `shared/infrastructure/config/SecurityConfig.java`.
- Inject `TimeProvider`; for user-facing dates use `users/application/services/UserLocalDateService`. `nowLocal()` is deprecated, and direct `LocalDateTime.now()` / `LocalDate.now()` breaks tests and timezone behavior.
- Schema change = new `V<n>__*.sql` in `backend/src/main/resources/db/migration/`; never edit an applied migration. Hibernate uses `ddl-auto: validate`, so the entity must match the migration exactly.
- Changing the data-export schema requires bumping `ExportVersion.CURRENT` and extending its `MigrationChain` (`dataexport/domain/model/ExportVersion.java`); the chain is validated at startup.
- Investments is an append-only activity ledger: activities are audited and never physically deleted, edits rewrite derived state by replay, and holdings can legitimately be `HISTORY_INCOMPLETE`. Do not "simplify" it into CRUD.
- Money uses `BigDecimal` with explicit scale/rounding; do not introduce `double`.
- Backend messages, comments, logs and Javadoc are Spanish; keep that convention.

## Frontend rules

- pnpm only (`preinstall` enforces it). `pnpm-workspace.yaml` sets `minimumReleaseAge: 10080` and `trustPolicy: no-downgrade`, so a brand-new dependency version may refuse to install.
- TypeScript is strict with `verbatimModuleSyntax` and `noUnusedLocals`/`noUnusedParameters`: use `import type` for types and remove unused symbols or `pnpm type-check` fails.
- All user-facing strings go through i18next (`src/lib/i18n.ts`); locales are `de,en,es,fr,it,pt,ja`, fallback `en`. Add keys to every locale, not just `en`.
- API base URL is `/api/v1` (`src/shared/api/axiosClient.ts`) with `withCredentials: true`. A 401 emits the `auth:logout` window event; do not add ad-hoc redirects.
- Never persist tokens or secrets. `sessionCache` (sessionStorage) is explicitly for non-sensitive display data only.
- React Query keys are plain string arrays; mutations must invalidate every affected key (transactions touch dashboard metrics, history, budgets, category stats and recurring lists). See `features/transactions/hooks/useTransactionMutation.ts`.
- ESLint 10 with `--max-warnings 0`: `react-hooks/set-state-in-effect` and `react-refresh/only-export-components` warnings fail `pnpm lint`.

## Deployment / ops gotchas

- There is no `application-prod.yml`. Production config comes from `SPRING_*` env vars in `docker-compose.yml`; do not add a profile file and assume it is used.
- The backend container healthcheck calls `/actuator/health`, but `spring-boot-starter-actuator` is not a dependency, so the container reports unhealthy even when the app works. Do not trust that status.
- `verdict` (AI assistant UI) runs only under compose profile `ai` and needs an OpenAI-compatible endpoint via `MILLETE_AI_BASE_URL` (default Ollama `qwen3:4b-instruct`).
- The frontend is served by its own nginx container; nginx reverse-proxies `/api` to the backend. Do not hardcode `localhost:8080` in client code.

## Documentation is often stale

`README.md`, `SECURITY.md`, `documentation/`, `CHANGELOG.md`, `TODO` and `REVISION` describe older versions (e.g. `application-prod.yml`, shadcn, JWT in sessionStorage, enforced CSP, backend port not exposed). Trust code, config and migrations over these files.

## Baseline failures (branch v0.2.0)

Do not assume the tree is clean or that these are your regressions:

- `frontend`: `pnpm lint` fails on `ColorPicker.tsx`, `RecordActivityDialog.tsx`, `TimezoneSection.tsx`, `NewTransactionDialog.tsx`, `useServerPagination.ts` and `i18nFormat.ts`; warnings exist in `AuthContext.tsx`, `LocalCurrencySection.tsx` and `useCountUp.ts`.
- `backend`: `mvn test` fails at test compilation because `InvestmentExportAdapterTest` / `InvestmentImportAdapterTest` reference removed types (`InvestmentSnapshot`, `Investment`, `InvestmentRepository`). Main code compiles and packages fine.
- Uncommitted work on `v0.2.0` touches investments market data and frontend investments; leave it alone unless asked.

## Verification before finishing

Run `pnpm type-check && pnpm build` and `mvn "-Dmaven.test.skip=true" package`. There is no CI, no pre-commit config and no frontend test suite, so these local commands are the safety net.
