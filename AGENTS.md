# AGENTS.md — Millete

> **Project:** Millete — Personal Finance Enterprise Web App  
> **Version:** 0.1.0  
> **Last updated:** 2026-07-01  
> **Language:** English (project documentation and comments are mainly in English and Spanish)

---

## 1. Project Overview

Millete is a production-ready, self-hosted personal finance platform. It tracks income/expenses, manages automated recurring bills, monitors investment portfolios, and enables secure family-unit collaboration.

The project is a **full-stack monorepo** with a decoupled architecture:

- **Backend:** Java 25 + Spring Boot 4.x, following **Hexagonal Architecture (Ports & Adapters)** and **Domain-Driven Design (DDD)**.
- **Frontend:** React 19 + TypeScript + Vite, using **TanStack Query** for server-state and **Tailwind CSS + Shadcn/ui** for styling.
- **Database:** PostgreSQL 16 with **Flyway** evolutionary migrations.
- **Deployment:** Docker Compose with Nginx reverse proxy, automated daily backups, and rootless containers.

---

## 2. Repository Structure

```
├── backend/              # Spring Boot application (Maven)
│   ├── src/main/java/  # Java source code (hexagonal/DDD modules)
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   └── db/migration/   # Flyway SQL migrations
│   ├── src/test/java/      # Unit & integration tests
│   ├── Dockerfile
│   └── pom.xml
├── frontend/           # React SPA (pnpm + Vite)
│   ├── src/
│   │   ├── app/        # Router guards, global styles
│   │   ├── features/   # Domain-driven feature modules
│   │   ├── shared/     # API client, components, hooks, utils
│   │   ├── lib/        # i18n setup, cn() helper
│   │   └── assets/     # Static assets, i18n JSON bundles
│   ├── Dockerfile
│   ├── nginx.conf
│   ├── vite.config.ts
│   ├── tsconfig.json
│   ├── eslint.config.js
│   └── package.json
├── documentation/      # Markdown docs per module (back/ + front/)
├── scripts/            # DB init & restore shell scripts
├── docker-compose.yml
├── manage.sh           # Unified management script
├── .env.example
└── backups/            # PostgreSQL dump storage
```

---

## 3. Technology Stack

### Backend
| Layer | Technology |
|-------|------------|
| Language | Java 25 (LTS) |
| Framework | Spring Boot 4.0.6 |
| Security | Spring Security, JWT (JJWT 0.12.6), BCrypt |
| Persistence | Spring Data JPA, Hibernate, PostgreSQL 16 |
| Migrations | Flyway |
| Build | Maven 3.x |
| Mapping | MapStruct 1.6.3 + Lombok 1.18.40 |
| PDF/Export | Apache PDFBox 3.0.4, Flying Saucer, Thymeleaf |
| CSV | Apache Commons CSV 1.12.0 |
| Encryption | Jasypt 3.0.5 (optional, for `application-prod.yml`) |

### Frontend
| Layer | Technology |
|-------|------------|
| Framework | React 19.2.5 |
| Language | TypeScript 6.0.2 |
| Build | Vite 8.0.10 |
| State | TanStack Query (React Query) 5.100.8 |
| Routing | React Router DOM 7.14.2 |
| Styling | Tailwind CSS 4.2.4 |
| UI Kit | Shadcn/ui style (Radix UI + CVA + Framer Motion) |
| Forms | React Hook Form 7.75.0 + Zod 4.4.3 |
| i18n | i18next 26.0.8 (7 languages: de, en, es, fr, it, pt, ja) |
| Icons | lucide-react |
| Notifications | sonner |
| Package Manager | pnpm 11.7.0 (enforced) |

### Infrastructure
| Layer | Technology |
|-------|------------|
| Orchestration | Docker Compose |
| Reverse Proxy | Nginx 1.27 (Alpine, rootless) |
| Database | PostgreSQL 16 (Alpine) |
| Backups | PostgreSQL sidecar container, daily at 02:00, 7-day retention |

---

## 4. Build & Run Commands

### Backend (Maven)
```bash
# Compile & package (skips tests)
cd backend && ./mvnw clean package -DskipTests

# Run tests
cd backend && ./mvnw test

# Run locally (requires PostgreSQL on localhost:5432)
cd backend && ./mvnw spring-boot:run
```

### Frontend (pnpm)
```bash
cd frontend

# Install dependencies
pnpm install

# Development server (port 5173)
pnpm run dev

# Type check
pnpm run type-check

# Build for production
pnpm run build

# Lint (zero warnings allowed in CI)
pnpm run lint

# Bundle analysis
pnpm run analyze
```

### Docker Compose (Full Stack)
```bash
# 1. Copy environment file and edit values
cp .env.example .env

# 2. Initialize volumes & permissions
sh manage.sh init

# 3. Start all services
sh manage.sh start

# 4. View logs
sh manage.sh logs backend

# 5. Manual backup
sh manage.sh backup-now

# 6. Restore from backup
sh manage.sh restore
```

### `manage.sh` Reference
| Command | Description |
|---------|-------------|
| `start` | Start all services in background |
| `stop` | Stop services (preserve data) |
| `restart` | Restart containers (ignores config changes) |
| `reload` | Rebuild images & recreate containers |
| `down` | Stop and remove containers/networks |
| `status` | Container health + recent backups |
| `logs [svc]` | Tail logs (optionally filter by service) |
| `backup-now` | Trigger manual DB backup |
| `restore` | Interactive DB restoration wizard |
| `init` | Bootstrap Docker volume & permissions |
| `create-app-user` | Create `millete_app` DB user |
| `clean-all` | **DESTRUCTIVE** — wipe containers, volumes, backups |

---

## 5. Code Organization

### Backend — Hexagonal / DDD

Each feature module is a **bounded context** with a consistent vertical slice:

```
com.puntomartinez.millete.<module>/
├── domain/
│   ├── model/              # Domain entities (rich models, zero framework deps)
│   ├── ports/
│   │   ├── in/             # Input ports (UseCase interfaces, Command records)
│   │   └── out/            # Output ports (Repository interfaces, external ports)
│   └── utils/              # Domain utilities
├── application/
│   └── services/           # Application services implementing use cases
├── infrastructure/
│   ├── in/
│   │   └── controller/     # REST controllers + Request/Response DTOs
│   └── out/
│       └── persistence/
│           └── postgresql/ # JPA entities, Spring Data repos, MapStruct mappers, adapters
```

**Feature modules:** `categories`, `dashboard`, `dataexport`, `groupgoals`, `investments`, `notifications`, `plannedtransactions`, `savingsgoals`, `transactions`, `users`.

**Shared cross-cutting concerns:** `shared.infrastructure.config` (Security, JWT, CORS, rate limiting, scheduling), `shared.infrastructure.in.controller.advice` (global exception handler).

**Key patterns:**
- Controllers map DTOs → Commands → Domain → Ports.
- Application services orchestrate domain logic; they do **not** depend on frameworks.
- Adapters (e.g., `CategoryPostgresAdapter`) implement out-ports and depend on JPA.
- Soft deletes use an `active` boolean + `@SQLRestriction("active = true")` on JPA entities.

### Frontend — Feature-Based Architecture

```
src/
├── app/              # Router guards (ProtectedRoute, PublicRoute), globals.css
├── features/         # One folder per domain feature
│   └── <feature>/
│       ├── components/
│       ├── hooks/          # TanStack Query hooks (queries + mutations)
│       ├── pages/          # Route-level page components
│       ├── services/       # Thin Axios wrappers
│       ├── types/          # Feature-specific TypeScript types
│       ├── schemas/        # Zod validation schemas
│       └── context/        # React context (e.g., AuthContext)
├── shared/
│   ├── api/          # Axios client (axiosClient.ts)
│   ├── components/   # Reusable UI components (core/ = shadcn-style)
│   ├── config/       # Navigation registry
│   ├── constants/
│   ├── hooks/        # Shared custom hooks
│   ├── themes/       # Theme definitions
│   ├── types/        # Shared TypeScript types
│   └── utils/        # Formatting, storage, notifications, i18n helpers
└── lib/              # i18n.ts, utils.ts (cn helper)
```

**Key patterns:**
- All non-critical routes are lazy-loaded (`React.lazy`) for code splitting.
- Mutations invalidate multiple query keys to keep dashboard charts and lists in sync.
- `cn(...)` (clsx + tailwind-merge) is used everywhere for conditional class merging.
- Path alias `@/` maps to `./src`.

---

## 6. Code Style Guidelines

### Java (Backend)
- **Java 25** syntax and features.
- **Lombok** is used for boilerplate reduction (`@Getter`, `@Builder`, etc.).
- **MapStruct** for entity ↔ domain mapping. Annotation processor order matters: **Lombok → lombok-mapstruct-binding → MapStruct**.
- Domain entities are **rich models** with validation in constructors (no framework annotations).
- JPA entities are separate from domain models and live in `infrastructure.out.persistence.postgresql`.
- Use immutable **Command records** for input ports.
- Controllers use `@Valid` on request DTOs.
- All env-sensitive values are externalized; Jasypt (`ENC(...)`) is optional for production.

### TypeScript / React (Frontend)
- **TypeScript 6** with strict linting (`noUnusedLocals`, `noUnusedParameters`, `erasableSyntaxOnly`).
- **Functional components** only; hooks follow React conventions.
- **ESLint flat config** with the following rules:
  - `no-console` → warn (allow `warn` and `error`)
  - `no-debugger` → warn
  - `@typescript-eslint/no-explicit-any` → warn
  - `react-refresh/only-export-components` → warn
  - `react-hooks/set-state-in-effect` → warn
- **Zero warnings** are allowed in CI (`pnpm run lint` uses `--max-warnings 0`).
- **Tailwind CSS v4** with `@theme` blocks in `index.css`.
- **Shadcn/ui components** are custom-built in `shared/components/core/` using Radix primitives + CVA.
- i18n keys are fully type-safe via `shared/types/i18n.d.ts`.

---

## 7. Testing Instructions

### Backend
- Tests are located in `backend/src/test/java/` and mirror the source package structure.
- Run with: `./mvnw test`
- **29 test files** covering domain models, application services, and some controllers.
- Key modules tested: `categories`, `dashboard`, `dataexport`, `groupgoals`, `investments`, `notifications`, `plannedtransactions`, `savingsgoals`, `transactions`, `users`, and `shared` (global exception handler).

### Frontend
- **No testing framework is currently configured.** There are no test files, no Jest/Vitest/Playwright configs, and no testing dependencies in `package.json`.
- If you add tests, follow the existing feature-folder structure and place tests alongside the code or in a `__tests__` subfolder.

---

## 8. Database & Migrations

- **Engine:** PostgreSQL 16 (Alpine).
- **Schema management:** Flyway with `ddl-auto: validate` in all environments. Hibernate never auto-creates tables.
- **Migrations location:** `backend/src/main/resources/db/migration/`
- **Naming convention:** `V{version}__{description}.sql`
- **Existing migrations:**
  - `V1__initial_schema.sql` — base tables (users, categories, transactions, investments, family_*).
  - `V2__v0.1.0.sql` — adds user preferences, sessions, savings goals, notifications; renames `family_*` → `goal_*`.
  - `V3__multi_session_support.sql` — removes unique constraint on `user_sessions`.
- **Application user:** `millete_app` is created automatically by `scripts/init-app-user.sh` with minimal privileges (`SELECT, INSERT, UPDATE, DELETE` on all tables).
- **Backups:** Automated daily at 02:00 with 7-day retention. Manual backup via `sh manage.sh backup-now`.

---

## 9. Security Considerations

### Authentication & Authorization
- **JWT tokens** (HMAC-SHA256 via JJWT) with 12-hour expiration.
- **Session binding:** Each token includes a `sessionId` claim validated against the database on every request. Revoked sessions are rejected immediately.
- **Password hashing:** BCrypt with default strength (10 rounds).
- **Brute-force protection:**
  1. IP-based rate limiting: 5 failed attempts per minute (`LoginRateLimitFilter`).
  2. Account locking: 5 failed attempts → 15-minute block persisted in `user_sessions`.
- **Method-level security:** `@EnableMethodSecurity` is active; `@PreAuthorize` can be used on service methods.

### Anti-IDOR
Every database query that touches user-owned data **must** validate resource ownership against the authenticated `userId` extracted from the JWT (`authentication.getName()`). This is enforced at the adapter level, not just the controller level. No exceptions are made for admin or system operations.

### Infrastructure
- **Nginx** runs rootless (Alpine) with security headers (`X-Frame-Options`, `X-Content-Type-Options`, `Referrer-Policy`, `Permissions-Policy`).
- **Client body size** limited to 10 MB.
- **CORS** configured via `CORS_ALLOWED_ORIGINS` environment variable.
- **No container runs as root.** Backend uses UID 1001 (`millete`), frontend uses UID 1001 (`millete-user`), backup uses UID 1000.
- **JWT secret** and **Jasypt password** must be kept out of version control (use `.env`).

### Client-Side
- Token stored in `sessionStorage` (prefixed `ms_`) — cleared when the tab closes.
- On `401 Unauthorized`, storage is cleared and the user is redirected to `/login`.
- React JSX escaping mitigates XSS; no `dangerouslySetInnerHTML` is used for dynamic user content.

---

## 10. Environment Variables

Copy `.env.example` to `.env` and configure the following:

| Variable | Purpose | Example |
|----------|---------|---------|
| `DATABASE_NAME` | PostgreSQL database name | `millete_db` |
| `DATABASE_USER` / `DATABASE_PASSWORD` | App DB credentials | `millete_app` / `...` |
| `POSTGRES_SUPERUSER_PASSWORD` | Postgres superuser password | `...` |
| `JASYPT_ENCRYPTOR_PASSWORD` | Master key for Jasypt encryption | `CHANGE_ME_JASYPT` |
| `JWT_SECRET` | HMAC secret for JWT signing | `openssl rand -base64 64` |
| `CORS_ALLOWED_ORIGINS` | Allowed frontend origins | `http://localhost:3000` |
| `SPRING_PROFILES_ACTIVE` | Spring profile | `prod` |
| `LOG_LEVEL` | Logging level | `INFO` |
| `VITE_API_URL` | Frontend API base URL | `/api/v1` |
| `VITE_APP_VERSION` | Frontend build version | `0.1.0` |
| `FRONTEND_PORT` | Exposed host port for Nginx | `3000` |

> **Security Note:** Generate strong passwords with `openssl rand -base64 32`.

---

## 11. Deployment Notes

- The backend port (`8080`) is **not exposed** to the host. All API traffic flows through Nginx at `/api/v1`.
- The frontend container serves the built SPA on port `80` and proxies `/api/` to the backend.
- The external Docker volume `millete_postgres_data` persists data across container rebuilds.
- For production, use `SPRING_PROFILES_ACTIVE=prod` and consider enabling Jasypt for encrypted credentials in `application-prod.yml`.
- Automated daily backups run at 2:00 AM with a 7-day retention policy.
- All containers run as non-root users for security.

---

## 12. Useful Documentation

Additional module-level documentation exists under `documentation/`:

- `documentation/back/` — Backend module docs (users, transactions, investments, categories, dashboard, group goals, savings goals, planned transactions, import/export, shared infrastructure).
- `documentation/front/` — Frontend module docs (auth, dashboard, transactions, categories, investments, group goals, savings goals, profile, wiki, shared components, routing, libraries).

These are written in a mix of English and Spanish and describe design decisions, data flows, and component hierarchies per feature.

---

## 13. Commit Convention

The project follows [Conventional Commits](https://www.conventionalcommits.org/):

- `feat:` — new feature
- `fix:` — bug fix
- `docs:` — documentation changes
- `refactor:` — code changes without new functionality
- `test:` — adding or updating tests

---

*For questions or clarifications, refer to `README.md`, `SECURITY.md`, and `CONTRIBUTING.md` in the project root.*
