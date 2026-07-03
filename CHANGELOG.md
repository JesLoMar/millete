# Changelog

All notable changes to this project will be documented in this file.
The format is based on [Keep a Changelog](https://keepachangelog.com/).

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