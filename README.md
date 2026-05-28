# Millete – Personal Finance Web App

**Millete** is an all-in-one personal finance platform that helps you track income and expenses, manage recurring bills, monitor investments, and collaborate with your family — all in one place.

Live at: [https://www.millete.online](https://www.millete.online)

---

## Key Features

### Dashboard
Get a bird's-eye view of your financial health:
- Total balance, income, expenses, and savings with trend indicators.
- Interactive charts for spending history, category breakdown, and budget tracking.
- Recent transactions and investment metrics.

### Transaction Management
- Add, edit, or delete income and expense records.
- Categorize transactions (Food, Housing, Transport, Utilities, Leisure).
- Search, filter by type (all, income, expense), and paginate through your history.
- Smart budget guardian: alerts when expenses exceed 70% of monthly income.

### Recurring Transactions
- Set up daily, weekly, monthly, or yearly recurring transactions.
- Define start date, optional end date, and interval.
- Automatically create real transactions on schedule via daily background processing.

### Categories
- Create custom categories with names, colors, and optional budget limits.
- Edit or soft-delete categories without losing historical transaction data.
- Categories help organize spending and power the budget tracking feature.

### Investments
- Track stocks, crypto, funds, real estate, and other assets.
- Record quantity, purchase price, purchase date, and ticker symbol.
- Automatically calculate invested capital, current value, profit/loss, and ROI percentage.
- Update market prices to see real-time portfolio performance.

### Family Collaboration
- Create a family unit with a monthly contribution goal.
- Invite family members by email (48-hour expiration links).
- Distribute contributions equally, proportionally by salary, or with custom percentages.
- Track each member's contribution history.

### Data Export & Import
- Export all your data (categories, transactions, planned transactions, investments) as a JSON file.
- Import previously exported data to restore or migrate your account.
- Versioned format with automatic migration for future updates.
- Ownership validation prevents importing another user's data.

---

## Technology Stack

### Frontend
- React with TypeScript
- Tailwind CSS for styling
- TanStack Query for data fetching and caching
- Recharts for data visualizations
- Shadcn/ui component library

### Backend
- Java 17 with Spring Boot 3
- Spring Security with JWT authentication
- PostgreSQL database
- Flyway for schema migrations
- MapStruct for entity mapping
- Hexagonal architecture (ports and adapters)

### Infrastructure
- Maven for build automation
- JPA / Hibernate for ORM
- BCrypt for password hashing
- JavaMailSender for email invitations (Brevo SMTP)
- Scheduled tasks for recurring transactions

---

## API Overview

The backend REST API is organized by domain:

- **Authentication** – POST /api/v1/auth/register, POST /api/v1/auth/login, GET /api/v1/auth/me/topnav
- **Transactions** – GET, POST, PUT, DELETE /api/v1/transactions + GET /metrics
- **Categories** – GET, POST, PUT, DELETE /api/v1/categories
- **Planned Transactions** – GET, POST, PUT, DELETE /api/v1/planned-transactions
- **Investments** – GET, POST, PATCH /price, DELETE /api/v1/investments
- **Dashboard** – GET /metrics, /history, /categories, /budgets, /recent-transactions, etc.
- **Family** – GET, POST, PUT, DELETE /api/v1/families + invitations and contributions
- **Data** – GET /api/v1/data/export, POST /api/v1/data/import

All endpoints, except authentication endpoints, require a valid JWT bearer token.

---

## Database Schema

The application uses 9 main tables with soft delete support (active flag):

- users – account information with anonymization support
- categories – user-defined spending categories
- transactions – all income and expense records
- planned_transactions – recurring transaction templates
- investments – asset holdings
- family_units, family_members, family_invitations, family_contributions

Foreign keys use CASCADE for ownership and SET NULL for optional relationships (e.g., transactions when a category is deleted).

---

## Security Highlights

- JWT-based authentication with 12-hour expiration
- BCrypt password hashing (no plain-text storage)
- Anti-IDOR protection: every request validates resource ownership
- Soft delete pattern preserves data history
- Anonymization support for user data upon account deletion
- Export files are ownership-validated before import

---

## Known Limitations

- Recurring transaction logic is currently a placeholder (only start date is evaluated). Full interval-based scheduling is under development.
- Investment price updates are manual; no automatic price feeds are integrated.
- Family invitations expire after 48 hours; no resend functionality yet.
- The export format is currently at version 0.0.1; future major versions may break backward compatibility.

---

## License

This project is licensed under the MIT License with Commons Clause.

- You are free to use, copy, modify, and distribute this software for **non-commercial purposes only**.
- Commercial use — including but not limited to selling, licensing, hosting as a service (SaaS), or including the software in a commercial product — is **strictly prohibited** without explicit permission from the author.
- You must give appropriate credit to the original author and retain all copyright notices.
- The software is provided "as is", without warranty of any kind.

**MIT License with Commons Clause**

Copyright (c) 2026 JesLopMar

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, and sublicense copies of the Software, **subject to the following condition**:

The Software may not be used for commercial purposes. Commercial purposes means the intended use of the Software or the output of the Software in any activity that generates revenue, including but not limited to:

- Selling the Software or any derivative work
- Providing the Software as a hosted service (SaaS, PaaS, etc.)
- Using the Software in a commercial product or service

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.

---

## Contact

For questions or support, visit [https://www.millete.online](https://www.millete.online)