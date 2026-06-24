# Millete – Agent Guide

> **Target reader:** AI coding agents with zero prior knowledge of this project.  
> **Language:** Spanish (the language used in code comments, documentation and commit messages).  
> **Last updated:** 2026-06-24

---

## 1. Project Overview

**Millete** is a production-ready, self-hosted personal finance web application. It tracks income/expenses, manages automated recurring bills, monitors investment portfolios, and enables secure family-unit collaboration.

- **Live site:** https://www.millete.online
- **Current version:** 0.1.0
- **Architecture:** Hexagonal / Domain-Driven Design (DDD) — pure domain core with zero framework dependencies.
- **Security:** Strict anti-IDOR layer — every database transaction validates cross-entity resource ownership dynamically against the authenticated context.

### Repository layout

```
.
├── backend/          # Spring Boot 4.x API (Java 25)
├── frontend/         # React 19 SPA (TypeScript + Vite)
├── documentation/    # Markdown docs per module (back/ & front/)
├── scripts/          # backup.sh & restore.sh
├── docker-compose.yml
└── manage.sh         # Docker orchestration helper
```

---

## 2. Technology Stack

### Backend
| Layer | Technology |
|-------|------------|
| Language | Java 25 (LTS) |
| Framework | Spring Boot 4.0.6 |
| Security | Spring Security, stateless JWT (12 h expiration), BCrypt |
| Persistence | PostgreSQL 16, Hibernate ORM, Flyway migrations |
| Build tool | Maven (wrapper: `mvnw` / `mvnw.cmd`) |
| Utilities | Lombok, MapStruct, JJWT, Jasypt (encryption), Apache Commons CSV, PDFBox, Flying Saucer PDF, Thymeleaf |

### Frontend
| Layer | Technology |
|-------|------------|
| Framework | React 19.2.5 + TypeScript 6.0 |
| Bundler | Vite 8 |
| Styling | Tailwind CSS 4.2.4 |
| UI kit | Shadcn/ui (style: new-york), Radix UI primitives, Lucide icons |
| State (server) | TanStack Query (React Query) v5 |
| Forms | React Hook Form + Zod v4 |
| i18n | i18next + react-i18next (7 languages: es, en, de, fr, it, pt, ja) |
| Notifications | Sonner |
| Package manager | **pnpm 11.7.0** (npm/yarn are blocked) |

### Infrastructure
| Layer | Technology |
|-------|------------|
| Containerisation | Docker + Docker Compose |
| Reverse proxy | Nginx 1.27 (rootless, Alpine) |
| Database backups | Cron-like shell script inside a `postgres:16-alpine` sidecar |

---

## 3. Build & Run Commands

### Prerequisites
- Docker & Docker Compose
- (Optional for local dev) JDK 25 + Maven 3.9+ for backend; Node 20 + pnpm 11+ for frontend

### Quick start (Docker)
```bash
# 1. Create the external volume once
sh manage.sh init

# 2. Start the whole stack
sh manage.sh start          # docker compose up -d

# 3. Check status
sh manage.sh status

# 4. View logs
sh manage.sh logs [service] # service = postgres | backend | frontend | db-backup

# 5. Stop / restart
sh manage.sh stop           # docker compose stop
sh manage.sh restart        # docker compose restart
sh manage.sh down           # remove containers (data preserved)
```

### Backend (local development)
```bash
cd backend

# Compile & run
./mvnw spring-boot:run

# Run tests
./mvnw test

# Package JAR
./mvnw clean package
```

### Frontend (local development)
```bash
cd frontend

# Install dependencies (pnpm is mandatory)
pnpm install

# Dev server (port 5173)
pnpm dev

# Type-check only
pnpm type-check

# Build for production
pnpm build

# Lint (zero warnings policy)
pnpm lint
pnpm lint:fix

# Preview production build
pnpm preview
```

---

## 4. Code Organisation

### Backend — Hexagonal modules

Each business module lives under `backend/src/main/java/com/puntomartinez/millete/<module>/` and follows the same layered structure:

```
<module>/
├── application/services/          # Application services (use-case orchestrators)
├── domain/
│   ├── model/                     # Entities, value objects, enums
│   ├── ports/in/                  # Input ports (use-case interfaces + commands)
│   ├── ports/out/                 # Output ports (repository interfaces)
│   └── utils/                     # Domain helpers
├── infrastructure/
│   ├── in/controller/             # REST controllers + DTOs
│   └── out/persistence/postgresql/ # JPA entities, adapters, Spring-Data repos, MapStruct mappers
```

**Modules (10):** `categories`, `dashboard`, `dataexport`, `groupgoals`, `investments`, `plannedtransactions`, `savingsgoals`, `shared`, `transactions`, `users`.

**Shared infrastructure** (`shared/infrastructure/`):
- `config/SecurityConfig.java` — CORS, JWT filter chain, BCrypt, stateless sessions.
- `config/filter/JwtAuthenticationFilter.java` — extracts & validates Bearer tokens.
- `config/filter/LoginRateLimitFilter.java` — in-memory rate limiting (5 attempts/min per IP).
- `config/scheduler/TransactionScheduler.java` — daily cron at 00:01 for recurring transactions.
- `in/controller/advice/GlobalExceptionHandler.java` — uniform `ErrorResponseDTO` for all errors.

### Frontend — Feature-based structure

```
frontend/src/
├── app/               # Router setup (ProtectedRoute, PublicRoute)
├── assets/            # Static images, i18n JSON files per language
├── features/          # One folder per domain feature
│   ├── auth/
│   ├── categories/
│   ├── dashboard/
│   ├── groupgoals/
│   ├── investments/
│   ├── profile/
│   ├── savingsgoals/
│   ├── transactions/
│   └── wiki/
├── lib/               # i18n init, utility functions
└── shared/            # Cross-cutting concerns
    ├── api/           # Axios client with interceptors (axiosClient.ts)
    ├── components/    # Reusable UI (core/ = low-level primitives)
    ├── config/        # Navigation config
    ├── hooks/         # Shared TanStack Query hooks
    ├── themes/        # Palette definitions
    ├── types/         # Global TypeScript types
    └── utils/         # Helpers (secureStorage, notifications, i18nFormat, etc.)
```

**Key conventions:**
- Alias `@` maps to `src/` (configured in `vite.config.ts` and `tsconfig.app.json`).
- shadcn/ui aliases: `components` → `@/shared/components`, `ui` → `@/shared/components/ui`, `utils` → `@/lib/utils`.
- Each feature contains its own `components/`, `hooks/`, `pages/`, `types/`, and optionally `services/` or `schemas/`.

---

## 5. Database & Migrations

- **Engine:** PostgreSQL 16
- **Schema management:** Flyway (evolutionary, not generated).
- **Migration files:** `backend/src/main/resources/db/migration/`
  - `V1__initial_schema.sql`
  - `V2__v0.1.0.sql`
  - `V3__update_sessions.sql`
- **ORM:** Hibernate with `ddl-auto: validate` (never auto-create in any environment).
- **Local dev credentials:** see `backend/src/main/resources/application.yml` (user `postgres` / `654321`, DB `millete_db`).

---

## 6. Testing Strategy

### Backend
- **Framework:** JUnit 5 (via `spring-boot-starter-test`).
- **Test count:** 17 test classes (unit + integration).
- **Structure:** mirrors the source tree under `backend/src/test/java/...`.
- **Coverage areas:**
  - Domain model unit tests (e.g., `CategoryTest`, `TransactionTest`, `InvestmentTest`).
  - Application service tests (e.g., `CategoryServiceTest`, `DashboardServiceTest`, `UserServiceTest`).
- **Run:** `./mvnw test`

### Frontend
- **No automated tests are currently present.** The project relies on:
  - Strict TypeScript (`tsc --noEmit`).
  - ESLint with zero-warnings policy (`eslint . --max-warnings 0`).
  - Manual QA and visual regression via the production deployment.

---

## 7. Code Style Guidelines

### Java (backend)
- **Package:** `com.puntomartinez.millete.<module>`.
- **Architecture rule:** Domain classes must **not** import Spring, JPA, or any framework annotation. Only `application` and `infrastructure` layers may depend on frameworks.
- **Mapping:** MapStruct is used for all entity/DTO conversions; Lombok for boilerplate reduction.
- **Validation:** Bean Validation (`@Valid`) on controllers; domain-level validation inside model/value objects.
- **Security:** Every repository adapter must verify resource ownership via the authenticated `userId` (extracted from JWT). No exceptions.

### TypeScript / React (frontend)
- **Linting:** ESLint flat config (`eslint.config.js`).
  - `no-console`: warn (only `console.warn` and `console.error` allowed).
  - `@typescript-eslint/no-explicit-any`: warn.
  - `react-refresh/only-export-components`: warn.
- **Imports:** Use `@/` aliases; avoid deep relative paths (`../../`).
- **Components:** Functional components; hooks colocated with features.
- **Styling:** Tailwind CSS v4 with CSS variables (`@theme` block in `index.css`). Dark mode via `.dark` class.
- **i18n keys:** Namespaced by feature (`auth:`, `dashboard:`, `transactions:`, etc.).

### Commits
Follow [Conventional Commits](https://www.conventionalcommits.org/):
- `feat:` — new feature
- `fix:` — bug fix
- `docs:` — documentation changes
- `refactor:` — code changes without new functionality
- `test:` — adding or updating tests

---

## 8. Security Considerations

- **Authentication:** Stateless JWT (12 h expiration). Token extracted from `Authorization: Bearer <token>` header.
- **Passwords:** BCrypt-hashed; never stored in plain text.
- **Rate limiting:** `LoginRateLimitFilter` blocks IPs after 5 failed login attempts per minute (429 Too Many Requests).
- **Account lockout:** `AccountLockedException` handler ready (423 Locked); backend service exists for 5 failed attempts → 15 min lockout.
- **CORS:** Configurable via `CORS_ALLOWED_ORIGINS` env var; defaults to `http://localhost:5173` in dev.
- **Secrets:** Jasypt encrypts DB credentials in production. Generate strong `JWT_SECRET` with `openssl rand -base64 64`.
- **Frontend storage:** JWT and user data are stored in `localStorage` with a lightweight XOR-based obfuscation (`secureStorage.ts`). Not encryption-grade, but prevents casual inspection.
- **IDOR prevention:** Every query that touches user data must include an ownership check against the authenticated user extracted from the JWT (`authentication.getName()`).

---

## 9. Deployment & Operations

### Docker Compose services
| Service | Image / Build | Ports | Purpose |
|---------|---------------|-------|---------|
| `postgres` | `postgres:16-alpine` | `5432` | Primary database |
| `backend` | Multi-stage build (`eclipse-temurin:25-jdk-jammy` → `25-jre-jammy`) | `8080` | Spring Boot API |
| `frontend` | Multi-stage build (`node:22-alpine` → `nginx:1.27-alpine`) | `80` / `3000` host | React SPA served by Nginx |
| `db-backup` | `postgres:16-alpine` | — | Daily 2 AM backups + retention (7 days) |

### Environment variables (`.env`)
Key variables consumed by `docker-compose.yml`:
- `DATABASE_NAME`, `DATABASE_USER`, `DATABASE_PASSWORD`
- `DB_PORT`, `APP_PORT`, `FRONTEND_PORT`
- `JASYPT_ENCRYPTOR_PASSWORD`
- `JWT_SECRET`
- `SPRING_PROFILES_ACTIVE` (default `prod`)
- `VITE_API_URL` (default `/api/v1`)
- `VITE_APP_VERSION`
- `LOG_LEVEL`

### Backup / Restore
- **Automatic:** Daily at 02:00 via `db-backup` container; 7-day retention.
- **Manual:** `sh manage.sh backup-now`
- **Restore:** `sh manage.sh restore` (interactive; stops backend, drops DB, restores selected backup, restarts backend).
- **Disaster reset:** `sh manage.sh clean-all` (requires typing `DELETE`).

### Nginx configuration (`frontend/nginx.conf`)
- Serves static SPA with `try_files $uri $uri/ /index.html`.
- Proxies `/api/` to `millete-backend:8080`.
- Gzip enabled for text assets.
- Security headers: `X-Frame-Options`, `X-Content-Type-Options`, `X-XSS-Protection`.
- Static assets cached 1 year; HTML never cached.

---

## 10. Useful Notes for Agents

- **Do not assume npm or yarn.** Always use `pnpm` inside `frontend/`.
- **Do not modify Flyway migrations that have already run in production.** Create a new `V<N>__description.sql` instead.
- **When adding a new backend module, replicate the exact hexagonal folder structure** (`application`, `domain`, `infrastructure`) so the architecture stays consistent.
- **When adding a new frontend feature, create a folder under `frontend/src/features/`** with `components/`, `hooks/`, `pages/`, `types/`, and register routes in `frontend/src/App.tsx`.
- **All API endpoints are prefixed with `/api/v1/`** (see Nginx proxy and Spring controllers).
- **The project is primarily documented in Spanish.** Keep comments, commit messages, and user-facing strings in Spanish unless they are inside i18n JSON files (which support multiple languages).
- **No frontend unit tests exist yet.** If you add any, place them next to the component/hook they test and use Vitest (already compatible with the Vite setup).
