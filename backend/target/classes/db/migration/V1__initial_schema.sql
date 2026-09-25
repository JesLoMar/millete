-- =============================================
-- Millete v0.0.1 - Initial Schema
-- =============================================

-- 1. USERS
CREATE TABLE users (
                       id UUID PRIMARY KEY,
                       username VARCHAR(100) UNIQUE,
                       email VARCHAR(255) UNIQUE,
                       password VARCHAR(255) NOT NULL,
                       created_at TIMESTAMP NOT NULL,
                       modified_at TIMESTAMP NOT NULL,
                       active BOOLEAN NOT NULL DEFAULT TRUE,
                       anonymized BOOLEAN NOT NULL DEFAULT FALSE,
                       CONSTRAINT chk_user_identity CHECK (username IS NOT NULL OR email IS NOT NULL)
);

-- 2. FAMILY UNITS
CREATE TABLE family_units (
                              id UUID PRIMARY KEY,
                              name VARCHAR(100) NOT NULL,
                              monthly_target DECIMAL(12, 2) DEFAULT 0.00,
                              distribution_mode VARCHAR(20) NOT NULL DEFAULT 'EQUITATIVE',
                              created_at TIMESTAMP NOT NULL,
                              modified_at TIMESTAMP NOT NULL,
                              active BOOLEAN NOT NULL DEFAULT TRUE,
                              CONSTRAINT chk_distribution_mode CHECK (distribution_mode IN ('EQUITATIVE', 'PROPORTIONAL', 'CUSTOM'))
);

-- 3. FAMILY MEMBERS
CREATE TABLE family_members (
                                id UUID PRIMARY KEY,
                                family_id UUID NOT NULL,
                                user_id UUID NOT NULL,
                                role VARCHAR(20) NOT NULL DEFAULT 'MEMBER',
                                salary DECIMAL(12, 2) DEFAULT 0.00,
                                custom_percentage DECIMAL(5, 2),
                                joined_at TIMESTAMP NOT NULL,
                                created_at TIMESTAMP NOT NULL,
                                modified_at TIMESTAMP NOT NULL,
                                active BOOLEAN NOT NULL DEFAULT TRUE,
                                CONSTRAINT uq_family_user UNIQUE (family_id, user_id),
                                CONSTRAINT chk_member_role CHECK (role IN ('ADMIN', 'MEMBER')),
                                CONSTRAINT fk_members_family FOREIGN KEY (family_id) REFERENCES family_units(id) ON DELETE CASCADE,
                                CONSTRAINT fk_members_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_family_members_family ON family_members(family_id);
CREATE INDEX idx_family_members_user ON family_members(user_id);

-- 4. FAMILY INVITATIONS
CREATE TABLE family_invitations (
                                    id UUID PRIMARY KEY,
                                    family_id UUID NOT NULL,
                                    email VARCHAR(255) NOT NULL,
                                    token VARCHAR(255) UNIQUE NOT NULL,
                                    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
                                    expires_at TIMESTAMP NOT NULL,
                                    created_at TIMESTAMP NOT NULL,
                                    modified_at TIMESTAMP NOT NULL,
                                    active BOOLEAN NOT NULL DEFAULT TRUE,
                                    CONSTRAINT chk_invitation_status CHECK (status IN ('PENDING', 'ACCEPTED', 'EXPIRED')),
                                    CONSTRAINT fk_invitations_family FOREIGN KEY (family_id) REFERENCES family_units(id) ON DELETE CASCADE
);

CREATE INDEX idx_family_invitations_token ON family_invitations(token);
CREATE INDEX idx_family_invitations_family ON family_invitations(family_id);

-- 5. FAMILY CONTRIBUTIONS
CREATE TABLE family_contributions (
                                      id UUID PRIMARY KEY,
                                      family_id UUID NOT NULL,
                                      user_id UUID NOT NULL,
                                      amount DECIMAL(10, 2) NOT NULL,
                                      date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                      created_at TIMESTAMP NOT NULL,
                                      modified_at TIMESTAMP NOT NULL,
                                      active BOOLEAN NOT NULL DEFAULT TRUE,
                                      CONSTRAINT fk_contributions_family FOREIGN KEY (family_id) REFERENCES family_units(id) ON DELETE CASCADE,
                                      CONSTRAINT fk_contributions_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_family_contributions_family ON family_contributions(family_id);

-- 6. CATEGORIES
CREATE TABLE categories (
                            id UUID PRIMARY KEY,
                            user_id UUID NOT NULL,
                            family_id UUID,
                            name VARCHAR(100) NOT NULL,
                            color VARCHAR(20),
                            budget_limit DECIMAL(10, 2) DEFAULT 0.00,
                            created_at TIMESTAMP NOT NULL,
                            modified_at TIMESTAMP NOT NULL,
                            active BOOLEAN NOT NULL DEFAULT TRUE,
                            CONSTRAINT chk_budget_limit CHECK (budget_limit >= 0),
                            CONSTRAINT fk_categories_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                            CONSTRAINT fk_categories_family FOREIGN KEY (family_id) REFERENCES family_units(id) ON DELETE SET NULL
);

CREATE INDEX idx_categories_user ON categories(user_id);

-- 7. TRANSACTIONS
CREATE TABLE transactions (
                              id UUID PRIMARY KEY,
                              user_id UUID NOT NULL,
                              category_id UUID,
                              amount DECIMAL(10, 2) NOT NULL,
                              date TIMESTAMP NOT NULL,
                              type VARCHAR(20) NOT NULL,
                              description VARCHAR(255),
                              created_at TIMESTAMP NOT NULL,
                              modified_at TIMESTAMP NOT NULL,
                              active BOOLEAN NOT NULL DEFAULT TRUE,
                              CONSTRAINT chk_transaction_type CHECK (type IN ('INCOME', 'EXPENSE')),
                              CONSTRAINT fk_transactions_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                              CONSTRAINT fk_transactions_category FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL
);

CREATE INDEX idx_transactions_user_date ON transactions(user_id, date);
CREATE INDEX idx_transactions_category ON transactions(category_id);

-- 8. PLANNED TRANSACTIONS
CREATE TABLE planned_transactions (
                                      id UUID PRIMARY KEY,
                                      user_id UUID NOT NULL,
                                      category_id UUID,
                                      amount DECIMAL(10, 2) NOT NULL,
                                      type VARCHAR(20) NOT NULL,
                                      description VARCHAR(255) NOT NULL,
                                      frequency_type VARCHAR(20) NOT NULL,
                                      frequency_interval INT NOT NULL,
                                      start_date DATE NOT NULL,
                                      end_date DATE,
                                      created_at TIMESTAMP NOT NULL,
                                      modified_at TIMESTAMP NOT NULL,
                                      active BOOLEAN NOT NULL DEFAULT TRUE,
                                      CONSTRAINT chk_planned_type CHECK (type IN ('INCOME', 'EXPENSE')),
                                      CONSTRAINT chk_frequency_type CHECK (frequency_type IN ('DAYS', 'WEEKS', 'MONTHS', 'YEARS')),
                                      CONSTRAINT chk_frequency_interval CHECK (frequency_interval > 0),
                                      CONSTRAINT fk_planned_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                                      CONSTRAINT fk_planned_category FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL
);

CREATE INDEX idx_planned_transactions_user ON planned_transactions(user_id);

-- 9. INVESTMENTS
CREATE TABLE investments (
                             id UUID PRIMARY KEY,
                             user_id UUID NOT NULL,
                             asset_name VARCHAR(100) NOT NULL,
                             ticker VARCHAR(20),
                             quantity DECIMAL(18, 8) NOT NULL,
                             purchase_price DECIMAL(18, 2) NOT NULL,
                             current_price DECIMAL(18, 2),
                             type VARCHAR(20) NOT NULL,
                             purchase_date TIMESTAMP NOT NULL,
                             created_at TIMESTAMP NOT NULL,
                             modified_at TIMESTAMP NOT NULL,
                             active BOOLEAN NOT NULL DEFAULT TRUE,
                             CONSTRAINT chk_investment_type CHECK (type IN ('STOCK', 'CRYPTO', 'FUND', 'REAL_ESTATE', 'OTHER')),
                             CONSTRAINT chk_quantity_positive CHECK (quantity > 0),
                             CONSTRAINT fk_investments_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_investments_user ON investments(user_id);