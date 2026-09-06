# Changelog

All notable changes to this project will be documented in this file.
The format is based on [Keep a Changelog](https://keepachangelog.com/).

---
## [0.1.4] - 2026-xx-xx

- Eliminar toda referencia al bot de telegram.
- Añadir el disparador de transacciones recurrentes.
- Preparar el backend para compilación nativa.

---

## [0.1.3] - 2026-08-24

### Security
- **Content-Security-Policy header:** Added a strict CSP in nginx (`default-src 'self'`, no `unsafe-eval`), validated first in Report-Only mode before enforcement.
- **Removed `dangerouslySetInnerHTML`:** The login greeting now uses the i18next `<Trans>` component, eliminating the stored-XSS vector through translation files.
- **Honest session caching:** Renamed `secureStorage` to `sessionCache` with a documented contract — non-sensitive data only; the real session lives in the HttpOnly cookie.
- **Session revocation hardening:** "Close other sessions" no longer sends the current `sessionId` from the client; the backend derives it from the HttpOnly cookie, so a tampered client cannot preserve arbitrary sessions.
- **Unified password policy:** A single shared schema enforces the 8-character minimum in both registration and password change (previously inconsistent: 8 vs 6).
- **Export filename sanitization:** Exported filenames are now sanitized and unified under the `millete_*` branding (removing the legacy `familybudget` name and raw config values in filenames).

### Added
- **Connection error screen:** Losing connectivity with the app already loaded now shows a proper error screen with a retry action, instead of entering a "zombie session" state with an invalid session.
- **Savings goal links:** Added URL validation for user-entered links in savings goals, plus a button to open them.
- **Refined mobile login view:** Recent news and the guide are now accessible from the mobile login screen. Added translations for aria-labels.
- **Accessible language declaration:** The document `lang` attribute now syncs dynamically with the active language (WCAG 3.1.1).

### Changed
- **Notification panel behavior:** Opening the bell no longer marks anything as read. Clicking a notification marks it as read and navigates; the always-visible X button removes it from the panel.
- **Centralized cache invalidation:** The 9 duplicated query-key invalidations across 6 transaction mutations are now a single shared helper.
- **Simplified invitation flow:** Removed redundant invalidation/refetch/setQueryData chains when accepting or rejecting invitations.
- **Single responsive toast system:** Replaced the two simultaneously mounted `Toaster` instances with one whose position adapts via `matchMedia`.
- **Shared currency formatting:** `SavingsGoalCard` now uses the shared `formatCurrency` utility instead of a hardcoded `es-ES`/EUR formatter.
- **Browser autofill theming:** Autofill styling is now pure CSS driven by theme tokens; the JS hook with timers and hardcoded hex colors was removed.
- **Contribution modal UX:** The form resets on every open and stays open when saving fails, allowing an immediate retry.
- Updated README.md images.

### Fixed
- **401 interceptor:** Now distinguishes "session expired" from "invalid credentials" — a failed login attempt no longer reloads the page and loses the user's input.
- **Forced logout without reload:** The interceptor emits the `auth:logout` event and the SPA redirects cleanly, instead of a full `window.location` reload.
- **Post-login redirect whitelist:** Now derived from the single source of routes; the redirect to group goals (which never worked) is fixed.
- **`user_sessions` table:** Deep correction of the table and related errors; aligned IP rate-limiting and session blockers.
- **Contribution history names:** Group goal contribution history now shows the real name of each contributing member (previously always "Member").
- **Invitation rejection UI:** Rejecting an invitation no longer renders as an error; it has its own state, icon, and message.
- **Recurring transaction edit modal:** The frequency selector appeared empty and sent wrong enum values to the backend (`DAILY`/`MONTHLY` vs the canonical `DAYS`/`MONTHS`); 8 non-existent translation keys were corrected to reuse existing ones.
- **Frozen header date and greeting:** Both now recalculate when the window regains focus; the week number is real ISO-8601.
- **Phantom period selector:** Removed the dead period state and selector from the savings goals page.
- **Stale draft in goal name dialog:** The edit-name dialog no longer shows a previous draft after saving and reopening.
- **Broken translation keys:** Fixed misreferenced keys shown raw in the UI (`.` instead of `:` separator): `savingsGoals.deleteGoalConfirmation`, `transactions.recurring.every`, `common:actions.remove`.
- **Dead Spanish fallbacks:** Removed `t(...) || 'fallback'` patterns (i18next returns the key, which is truthy) and added the real missing error keys in all 7 languages.
- **Hardcoded Spanish strings:** `EditGoalNameDialog` and `ImportModal` are now fully translated via keys.
- **Category selector** fixed in transaction creation modals; primary `dialog`, `select`, and `label` components fixed.
- Fixed the account deletion bug.
- Removed the icon from the category edit modal.
- Completed orphan translation keys across all languages.

### Performance
- **Extreme data volume handling:** Optimized queries and rendering for transactions, categories, savings goals, shared expense groups, contributions, and notifications.

---

## [0.1.2] - 2026-07-04

### Security
- **Transaction sign vulnerability:** Enforced positive-only amounts on the backend (`@Positive` validation) and frontend (`Math.abs()`), with domain-level rejection of non-positive values.
- **CSV formula injection:** Sanitized exported CSV values by prepending a single quote (`'`) to cells beginning with formula-triggering characters (`=`, `+`, `-`, `@`, `\t`, `\r`).
- **JWT secure cookies:** Migrated JWT storage from `sessionStorage` to `HttpOnly` secure cookies (`ms_token`), with `SameSite=Strict` and full backend cookie parsing support.

### Added
- **Official dark theme palette:** Implemented three complete theme variants:
  - **Dark Millete** 🌙 — warm bakery-at-dusk aesthetic (`#1A1208` base, `#3DAD8A` primary).
  - **Rosé Millete** 🌸 — elegant rose/white financial theme (`#FFF5F5` base, `#A03060` primary).
  - **Ember Millete** 🔥 — intense fire/black premium theme (`#080606` base, `#E63946` primary).
  - Full `localStorage` persistence and `prefers-color-scheme` detection via `useTheme.ts`.
- **Notification deep-linking:** Clicking a goal invitation notification now navigates directly to `/profile?section=notifications` and auto-scrolls to the `NotificationsTable`.
- **Notification badge dismissal:** Unread notification badge is now hidden locally when the notification dialog opens, without waiting for a backend round-trip.

### Fixed
- **Last admin protection:** Backend now forbids demoting or deleting the sole remaining admin of a group goal (`ForbiddenOperationException`). Frontend `EditMemberDialog` disables the MEMBER role option with a warning when `isLastAdmin=true`.
- **Group goal percentage accuracy:** Removed the desynced local `customPercentages` React state. `totalCustomPercentage` is now computed directly from `selectedGoal.members` backend data, so the `DistributionCard` and `EditMemberDialog` hints always show the real sum.
- **Translation key fix:** Changed `savingsGoals.addFunds` to the correct cross-namespace syntax `savingsGoals:addFunds` in `ContributionModal.tsx`.
- **Mobile investment menu:** Fixed the `AssetList` mobile layout so the three-dot menu and percentage badge are visible without horizontal scrolling (`min-w-100` → `min-w-0`, corrected Tailwind width classes).
- **Re-invitation after member deletion:** Fixed `acceptInvitation` in `GroupGoalService` to reactivate a soft-deleted member record instead of attempting a duplicate insert that violated the `uq_goal_user` constraint.
- **Dynamic percentage preview in edit modal:** The `EditMemberDialog` now shows a live projected total percentage (`dynamicTotal`) as the user edits the custom percentage field, with real-time valid/invalid color feedback.

---

## [0.1.1] - 2026-06-28

### Performance
- **Graphic asset optimization:** Converted images and UI assets to next-gen WebP format:
  - `favicon-96x96.png` (14.7 kB) -> `favicon-96x96.webp` (2.51 kB)
  - `web-app-icon.png` (264 kB) -> `web-app-icon.webp` (25.8 kB)
  - `web-app-manifest-192x192.png` (35.8 kB) -> `web-app-manifest-192x192.webp` (5.63 kB)
  - `web-app-manifest-512x512.png` (193 kB) -> `web-app-manifest-512x512.webp` (29.8 kB)
  - **Total asset reduction:** From 507.5 kB to 63.74 kB **(-87.44%)**
- **Initial JS bundle reduction (gzip):** Optimized build packaging, reducing size from ~260 KB to ~176 KB **(-32%)**.
- **Load times:** Enhanced initial load performance by **-33%** (from 2.1s down to 1.4s).
- **Global cleanup:** Reduced total frontend directory size from 2.69 MB to 1.53 MB **(-43.12%)**.

### Added
- Designed and implemented the first official color palette.
- Introduced micro-interactions to enhance user experience (UX).

### Fixed
- Fixed the `NotificationBell` component functionality (`src/features/notifications/components/NotificationBell.tsx`) inside the top navigation bar (`src/shared/components/TopNav.tsx`).
- Minor backend logic fixes.

---

## [0.1.0] - 2026-06-25
> **Note:** This version includes database migrations/schema changes.

### Added
- Implemented savings goals feature.
- Added ability to edit and delete Group Goals.
- Added last-execution persistence for recurring transactions.
- Native Telegram bot integration and dedicated API endpoints.
- Created user profile and Settings tab.
- Developed an in-app notification invitation system (removing the need for email verification).
- Added data export support for `.csv` and `.pdf` formats for spreadsheet viewing.

### Changed
- **Dependency migration:** Replaced third-party components (`shadcn`, `recharts`, and parts of `radix`) with custom native components.
- **Core refactoring:** Fully restructured and renamed the legacy `family` module to `GoalContribution`.
- Optimized and reduced localization keys, streamlining translation strings.
- Updated the import/export module to natively support the v0.1.0 data schema.

### Removed
- Cleaned up the project tree by removing redundant `.gitkeep` files.

---

## [0.0.4] - 2026-05-18

### Added
- Added Japanese language support.

### Changed
- Removed account restrictions when exporting data.

---

## [0.0.3] - 2026-04-30

### Security
- Added brute-force attack protection on the login form via IP-level banning.

### Fixed
- Ensured responsive design and proper layout rendering across Safari browsers.
- Resolved an authentication loop issue on incorrect application screens.
- Fixed request query logic to properly ignore deactivated categories and transactions.
- Added a fallback icon for orphan transactions whose categories had been deleted.
- Fixed the context menu behavior (three-dots options dropdown) for categories, transactions, and investments.
- Added deletion confirmation modals to prevent accidental data loss in transactions, recurring transactions, and investments.

### Changed
- Enhanced data persistence and save scripts.

---

## [0.0.2] - 2026-03-15

### Added
- Designed and deployed the project's dedicated Wiki.
- Added a time-calculation engine for recurring transactions logic.
- Built a robust categories module featuring IDOR protection, Bean Validation, Regex matching, and immutability.
- Added descriptive icons across the user interface.

### Changed
- **Architectural refactoring:** Unified all project mappers by migrating completely to MapStruct.
- Fixed outdated dependency versions in `pom.xml`.
- Optimized environment variable handling between repositories (`Dockerfile` and `docker-compose.yml`).
- Updated and redesigned the login page interface and information.
- General UI polished improvements.

### Fixed
- Fixed data consistency validation within the Family Distribution Mode.
- Fixed layout and loading errors on the Family tab.

### Removed
- Thoroughly stripped out debug logs from production environments.
