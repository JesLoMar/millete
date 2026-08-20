# Millete — Guide for AI Coding Agents

Millete is a self-hosted personal finance web application: income/expense tracking,
recurring bills, investment portfolios, savings goals, and family-unit collaboration.
It is a decoupled full-stack app: a Spring Boot REST API, a React SPA, and a
PostgreSQL database, orchestrated with Docker Compose.

Repository layout:

- `backend/` — Spring Boot 4.x / Java 25 API (Maven project).
- `frontend/` — React 19 + TypeScript SPA (pnpm project).
- `scripts/` — shell scripts for DB init/restore and data-generation utilities.
- `documentation/` — per-module docs (`back/` for backend modules, `front/` for
  frontend features) plus UI screenshots.
- `docker-compose.yml`, `manage.sh`, `.env.example` — deployment/ops at the root.

## Tech stack

Backend (`backend/pom.xml`):

- Java 25, Spring Boot 4.x (`spring-boot-starter-web`, `-security`, `-data-jpa`,
  `-validation`, `-flyway`, `-thymeleaf`).
- PostgreSQL 16 via Hibernate ORM; **Flyway** manages the schema
  (`backend/src/main/resources/db/migration/`, `ddl-auto: validate` — never let
  Hibernate create/alter the schema; write a new `V<n>__*.sql` migration instead).
- Auth: stateless JWT (jjwt 0.12.6, 12-hour expiry) stored in an HttpOnly cookie;
  BCrypt password hashing; Jasypt for config encryption.
- Lombok + MapStruct (with `lombok-mapstruct-binding`) as annotation processors.
- Apache Commons CSV, PDFBox, Flying Saucer (PDF) for exports/reports.

Frontend (`frontend/package.json`):

- React 19, TypeScript, Vite 8, Tailwind CSS 4, Shadcn/ui-style components on
  Radix UI primitives, Sonner toasts, framer-motion, lucide-react icons.
- Server state via **TanStack Query**; forms via react-hook-form + zod; axios
  client in `frontend/src/shared/api/axiosClient.ts`; routing with
  react-router-dom v7; i18next for translations.
- Package manager: **pnpm only** (`preinstall` enforces it; Node >= 20, pnpm >= 11).

## Architecture

### Backend — Hexagonal / DDD, package-by-feature

Base package: `com.puntomartinez.millete` under `backend/src/main/java`. Each
bounded context is a top-level package: `users`, `categories`, `transactions`,
`plannedtransactions`, `investments`, `savingsgoals`, `groupgoals`, `dashboard`,
`dataexport`, `notifications`, plus `shared`.

Every module follows the same three-layer hexagonal layout:

- `<module>/domain/model` — pure domain entities/value objects (no framework deps).
- `<module>/domain/ports/in` — use-case interfaces; `domain/ports/out` —
  repository/output ports.
- `<module>/application/services` — services implementing the `in` ports.
- `<module>/infrastructure/in/controller` — REST controllers + request/response DTOs.
- `<module>/infrastructure/out/persistence/postgresql` — JPA `*Entity`,
  Spring Data `*Repository`, `*PostgresAdapter` (implements the `out` port), and
  MapStruct `*EntityMapper`.

The `shared` module holds cross-cutting code: domain exceptions
(`DomainException` subclasses such as `ResourceNotFoundException`,
`ForbiddenOperationException`), the global exception handler,
`SecurityConfig`, `JwtAuthenticationFilter`, `LoginRateLimitFilter`, shared DTOs
(`JwtUser`, `PaginatedResponseDTO`, `ErrorResponseDTO`), and the daily
`TransactionScheduler` that materializes recurring transactions.

Key architectural constraints:

- The domain core must stay free of Spring/JPA dependencies.
- **Anti-IDOR rule:** every data access must validate resource ownership against
  the authenticated user context; ownership checks are a core part of business
  logic, not an afterthought.
- DTOs are mapped with MapStruct; controllers never expose JPA entities.

### Frontend — feature-based modules

`frontend/src` layout:

- `app/` — app shell: router and global CSS.
- `features/` — one folder per domain (`auth`, `categories`, `dashboard`,
  `transactions`, `investments`, `savingsgoals`, `groupgoals`, `notifications`,
  `profile`, `wiki`), each with `components/`, `hooks/`, `pages/`, `constants.ts`,
  `utils.ts`, and an `index.ts` barrel export.
- `shared/` — reusable `components/` (layout, metric cards, selectors, `core/`
  UI primitives), `api/axiosClient.ts`, `config/`, `constants/`, `hooks/`,
  `themes/`, `types/`, `utils/`.
- `lib/` — `i18n.ts` setup and generic utilities.

The SPA is served by Nginx, which proxies API calls to the backend under
`/api/v1` (configurable via `VITE_API_URL`).

## Build and test commands

Backend (run inside `backend/`, Maven wrapper included):

```bash
./mvnw clean package          # build jar (runs tests)
./mvnw test                   # run the JUnit test suite
./mvnw spring-boot:run        # run locally (dev profile, needs local PostgreSQL)
```

Frontend (run inside `frontend/`):

```bash
pnpm install
pnpm dev                      # Vite dev server
pnpm build:check              # tsc --noEmit + vite build (use before finishing work)
pnpm lint                     # ESLint, zero warnings allowed (--max-warnings 0)
pnpm doctor                   # react-doctor analysis
```

Full stack via Docker Compose (always through Git Bash on Windows):

```bash
cp .env.example .env          # then edit secrets
sh manage.sh init             # create volume millete_postgres_data + backups/ dir
sh manage.sh start            # build & start db, backend, frontend, backup services
sh manage.sh reload           # rebuild images after code changes
sh manage.sh logs backend     # tail logs
sh manage.sh status / backup-now / restore / clean-all
```

App URL: http://localhost:3000 (only the frontend port is exposed; backend 8080
and PostgreSQL 5432 stay on the internal `millete_network`).

## Testing instructions

- Backend: JUnit 5 via `spring-boot-starter-test`. Tests live in
  `backend/src/test/java` mirroring the module structure — unit tests for domain
  models and application services, plus controller tests (e.g.
  `DataExportControllerTest`). Run with `./mvnw test`.
- Frontend: no test framework is currently configured; verification is
  `pnpm lint` and `pnpm build:check` (strict TypeScript).
- There are no E2E tests; manual verification runs against the Docker stack.

## Code style and conventions

- Language of code, comments, and documentation is **English**.
- Backend: Lombok for boilerplate; constructor injection; MapStruct mappers for
  entity↔domain and DTO conversions; Bean Validation on request DTOs; throw the
  `shared` domain exceptions and let `GlobalExceptionHandler` render
  `ErrorResponseDTO`.
- Follow the existing hexagonal folder structure when adding a module — do not
  put framework annotations in `domain/` classes.
- Database changes: add a new Flyway migration in
  `backend/src/main/resources/db/migration/` (`V<n>__description.sql`); existing
  migrations are `V1__initial_schema.sql` and `V2__v0.1.0.sql`.
- Frontend: functional components + hooks; TanStack Query for all server state
  (no ad-hoc fetch state); react-hook-form + zod for forms; Tailwind utility
  classes with Shadcn/ui primitives; ESLint flat config with zero-warning policy.
- Use pnpm for the frontend — npm/yarn are blocked by `only-allow`.

## Security considerations

- Never commit `.env`; use `.env.example` as the template and generate secrets
  with `openssl rand -base64 32` (`JWT_SECRET`, `JASYPT_ENCRYPTOR_PASSWORD`, DB
  passwords).
- JWT travels in an HttpOnly, SameSite=Strict cookie (`cookie-secure` must be
  `true` in production/HTTPS); tokens expire after 12 hours.
- Login is rate-limited (`LoginRateLimitFilter`); keep it enabled.
- The backend port is intentionally not exposed in production; all traffic goes
  through the Nginx reverse proxy. CORS origins are whitelisted via
  `CORS_ALLOWED_ORIGINS`.
- The app connects to PostgreSQL as the least-privilege `millete_app` user
  (created by `scripts/init-app-user.sh`); the superuser is used only for
  backups. All containers run as non-root.
- Enforce the anti-IDOR ownership validation on every new endpoint that touches
  user data.
- `application.yml` contains local-dev defaults (dev profile, localhost DB);
  production values come from environment variables via Docker Compose.

## Where to learn more

`documentation/back/` has one doc per backend module (users, transactions,
categories, dashboard, group/saving goals, investments, planned transactions,
import/export, shared infrastructure, DB); `documentation/front/` does the same
for frontend features (auth, routing, shared components, i18n, etc.). Consult
the relevant doc before making non-trivial changes to a module.
