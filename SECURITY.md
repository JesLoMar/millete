# Security Policy

> **Project:** Millete — Personal Finance Platform  
> **Version:** 0.1.0  
> **Last updated:** 2026-06-25

---

## 1. Reporting a Vulnerability

If you discover a security vulnerability, **do NOT** open a public issue.

- **Email:** contact@millete.online
- **Expected response time:** within 48 hours
- **Please include:** a clear description, steps to reproduce, and potential impact assessment

We follow responsible disclosure. Once the vulnerability is confirmed and patched, we will credit the reporter (if desired) in the release notes.

---

## 2. Supported Versions

Security fixes are backported to all actively maintained versions:

| Version | Supported | Notes |
|---------|:---------:|-------|
| 0.1.x   | ✅        | Current stable — all security fixes |
| 0.0.x   | ⚠️        | Legacy — critical fixes only |

---

## 3. Security Architecture Overview

Millete implements a **defense-in-depth** strategy across every layer of the stack:

```
┌─────────────────────────────────────────────────────────────┐
│  Layer 1: Infrastructure (Nginx + Docker)                   │
│  → TLS, security headers, rate limiting, proxy hardening  │
├─────────────────────────────────────────────────────────────┤
│  Layer 2: Application (Spring Boot)                         │
│  → Stateless JWT, BCrypt, CORS, method-level security     │
├─────────────────────────────────────────────────────────────┤
│  Layer 3: Domain (Hexagonal Core)                           │
│  → Anti-IDOR ownership checks, input validation, DDD       │
├─────────────────────────────────────────────────────────────┤
│  Layer 4: Data (PostgreSQL)                                 │
│  → Encrypted credentials, Flyway migrations, no SQLi       │
├─────────────────────────────────────────────────────────────┤
│  Layer 5: Client (React SPA)                                │
│  → Session-scoped storage, XSS mitigation, secure logout     │
└─────────────────────────────────────────────────────────────┘
```

---

## 4. Authentication & Authorization

### 4.1 JWT Token Security
- **Algorithm:** RS256 (asymmetric) or HMAC-SHA256 (symmetric) via JJWT
- **Expiration:** 12 hours
- **Session binding:** Each token includes a `sessionId` claim validated against the database on every request. Revoked sessions are rejected immediately.
- **Secret generation:**
  ```bash
  openssl rand -base64 64
  ```
- **Storage:** Token is stored in `sessionStorage` (not `localStorage`) with a prefixed key (`ms_`) to reduce XSS persistence.

### 4.2 Password Security
- **Hashing:** BCrypt with default strength (10 rounds) via `BCryptPasswordEncoder`
- **Passwords are never stored in plain text** — not in logs, not in the database, not in memory dumps
- **Registration:** Validates presence of username or email; checks for duplicates before hashing

### 4.3 Account Lockout (Brute-Force Protection)
- **Failed login attempts:** 5 per minute per IP address
- **Lock duration:** 15 minutes
- **IP-based rate limiting:** `LoginRateLimitFilter` tracks attempts in a bounded in-memory map (max 10,000 IPs) with automatic eviction of expired windows
- **Reset on success:** Successful login clears all failed attempts for that account
- **HTTP response:** `429 Too Many Requests` with a `Retry-After` equivalent message

### 4.4 Method-Level Authorization
- `@EnableMethodSecurity` is active
- Controllers and service methods can be annotated with `@PreAuthorize` for fine-grained access control

---

## 5. Anti-IDOR (Insecure Direct Object Reference)

Every database query that touches user-owned data **must** validate resource ownership against the authenticated `userId` extracted from the JWT (`authentication.getName()`). This is enforced at the adapter level, not just the controller level.

Examples of protected resources:
- Transactions, categories, planned transactions
- Investments, savings goals
- Group goals, goal members, contributions
- User preferences and sessions

**No exception** is made for admin or system operations.

---

## 6. Infrastructure Security

### 6.1 Nginx Reverse Proxy

| Header | Value | Purpose |
|--------|-------|---------|
| `X-Frame-Options` | `SAMEORIGIN` | Clickjacking protection |
| `X-Content-Type-Options` | `nosniff` | MIME-type sniffing prevention |
| `X-XSS-Protection` | `1; mode=block` | Legacy XSS filter (defense in depth) |
| `server_tokens` | `off` | Hide Nginx version from error pages |

**Additional hardening:**
- Static assets cached 1 year (immutable files with content hashes)
- HTML never cached (`no-cache, no-store, must-revalidate`)
- Hidden files (`.git`, `.env`, etc.) denied at the web server level
- Client body size limited to 10 MB
- Docker network access restriction for `/nginx_status`

### 6.2 CORS Policy
- Configurable via `CORS_ALLOWED_ORIGINS` environment variable
- Defaults to `http://localhost:5173` in development
- Credentials allowed (`Access-Control-Allow-Credentials: true`)
- Exposed headers: `Authorization`
- Allowed methods: `GET, POST, PUT, PATCH, DELETE, OPTIONS`

### 6.3 Docker & Network
- Rootless Nginx (Alpine) in the frontend container
- PostgreSQL 16 Alpine with dedicated backup sidecar
- No containers run as root
- Internal service communication via Docker network (not exposed externally)

---

## 7. Data Protection

### 7.1 Credential Encryption (Jasypt)
Production database credentials and other secrets are encrypted at rest in `application-prod.yml` using Jasypt.

```bash
# Encrypt a value
mvn jasypt:encrypt -Djasypt.encryptor.password=$JASYPT_ENCRYPTOR_PASSWORD
```

**Required environment variable:** `JASYPT_ENCRYPTOR_PASSWORD`

### 7.2 Database Security
- **Engine:** PostgreSQL 16
- **Schema management:** Flyway (evolutionary migrations, never `ddl-auto: create`)
- **Validation:** `ddl-auto: validate` in all environments
- **Backups:** Automated daily at 02:00 with 7-day retention
- **Manual backup/restore:** `sh manage.sh backup-now` / `sh manage.sh restore`

### 7.3 Client-Side Storage
- JWT and user data stored in `sessionStorage` (cleared when the tab closes)
- Lightweight key prefixing (`ms_`) to avoid accidental collisions
- Automatic cleanup on parse errors or storage quota exceeded
- On `401 Unauthorized`: storage is cleared and the user is redirected to `/login`

---

## 8. Input Validation & Output Encoding

### 8.1 Backend
- Bean Validation (`@Valid`) on all controller inputs
- Domain-level validation inside model/value objects
- Global exception handler (`GlobalExceptionHandler`) returns uniform `ErrorResponseDTO` without leaking internal stack traces in production

### 8.2 Frontend
- React's built-in XSS protection via JSX escaping
- All user-generated content rendered through React components (no `dangerouslySetInnerHTML` for dynamic data)
- API responses validated with Zod schemas where applicable

---

## 9. Security Headers Checklist

When deploying to production, verify the following headers are present on all responses:

```bash
curl -I https://www.millete.online
```

Expected:
- `X-Frame-Options: SAMEORIGIN`
- `X-Content-Type-Options: nosniff`
- `X-XSS-Protection: 1; mode=block`
- `Strict-Transport-Security: max-age=63072000; includeSubDomains` *(recommended addition for HTTPS deployments)*

---

## 10. Incident Response

1. **Contain:** Isolate the affected service (`sh manage.sh stop`)
2. **Investigate:** Review logs (`sh manage.sh logs [service]`)
3. **Patch:** Apply the fix and run the full test suite
4. **Deploy:** Restart the stack (`sh manage.sh restart`)
5. **Communicate:** Notify affected users if personal data was involved

---

## 11. Security Best Practices for Operators

- [ ] Always use `application-prod.yml` in production
- [ ] Enable Jasypt and rotate the encryptor password periodically
- [ ] Generate a strong `JWT_SECRET` with `openssl rand -base64 64`
- [ ] Keep `JASYPT_ENCRYPTOR_PASSWORD` and `JWT_SECRET` out of version control (use `.env`)
- [ ] Run backups daily and test restore procedures quarterly
- [ ] Monitor `nginx_status` and application logs for anomalies
- [ ] Keep Docker images updated (`docker compose pull && docker compose up -d`)
- [ ] Review and prune old user sessions periodically

---

## 12. Compliance Notes

Millete is designed as a **self-hosted** personal finance application. While it is not certified for any specific regulatory framework, the following principles are applied:

- **Data minimization:** Only store what is strictly necessary for the feature set
- **Access control:** Users can only access their own data (anti-IDOR)
- **Session transparency:** Users can view and revoke active sessions
- **Account deletion:** Full account deactivation supported

---

*For questions or clarifications, contact: contact@millete.online*
