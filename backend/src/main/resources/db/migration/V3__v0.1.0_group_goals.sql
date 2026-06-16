-- ==============================================================
-- Millete v0.1.0.1 - Refactor family_* tables to goal_* naming
-- ==============================================================

-- 1. RENAME TABLES
ALTER TABLE family_units RENAME TO goal_units;
ALTER TABLE family_members RENAME TO goal_members;
ALTER TABLE family_invitations RENAME TO goal_invitations;
ALTER TABLE family_contributions RENAME TO goal_contributions;

-- 2. RENAME COLUMNS IN goal_members
ALTER TABLE goal_members RENAME COLUMN family_id TO goal_id;

-- 3. RENAME COLUMNS IN goal_invitations
ALTER TABLE goal_invitations RENAME COLUMN family_id TO goal_id;

-- 4. RENAME COLUMNS IN goal_contributions
ALTER TABLE goal_contributions RENAME COLUMN family_id TO goal_id;

-- 5. DROP OLD CONSTRAINTS ON goal_members
ALTER TABLE goal_members DROP CONSTRAINT uq_family_user;
ALTER TABLE goal_members DROP CONSTRAINT fk_members_family;
ALTER TABLE goal_members DROP CONSTRAINT fk_members_user;

-- 6. ADD NEW CONSTRAINTS ON goal_members
ALTER TABLE goal_members
    ADD CONSTRAINT uq_goal_user UNIQUE (goal_id, user_id),
ADD CONSTRAINT fk_members_goal FOREIGN KEY (goal_id) REFERENCES goal_units(id) ON DELETE CASCADE,
ADD CONSTRAINT fk_members_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

-- 7. DROP OLD CONSTRAINTS ON goal_invitations
ALTER TABLE goal_invitations DROP CONSTRAINT chk_invitation_status;
ALTER TABLE goal_invitations DROP CONSTRAINT fk_invitations_family;
ALTER TABLE goal_invitations DROP CONSTRAINT fk_invitations_inviter;
ALTER TABLE goal_invitations DROP CONSTRAINT fk_invitations_invited;

-- 8. ADD NEW CONSTRAINTS ON goal_invitations
ALTER TABLE goal_invitations
    ADD CONSTRAINT chk_invitation_status CHECK (status IN ('PENDING', 'ACCEPTED', 'REJECTED', 'EXPIRED')),
ADD CONSTRAINT fk_invitations_goal FOREIGN KEY (goal_id) REFERENCES goal_units(id) ON DELETE CASCADE,
ADD CONSTRAINT fk_invitations_inviter FOREIGN KEY (inviter_user_id) REFERENCES users(id) ON DELETE SET NULL,
ADD CONSTRAINT fk_invitations_invited FOREIGN KEY (invited_user_id) REFERENCES users(id) ON DELETE SET NULL;

-- 9. DROP OLD CONSTRAINTS ON goal_contributions
ALTER TABLE goal_contributions DROP CONSTRAINT fk_contributions_family;
ALTER TABLE goal_contributions DROP CONSTRAINT fk_contributions_user;

-- 10. ADD NEW CONSTRAINTS ON goal_contributions
ALTER TABLE goal_contributions
    ADD CONSTRAINT fk_contributions_goal FOREIGN KEY (goal_id) REFERENCES goal_units(id) ON DELETE CASCADE,
ADD CONSTRAINT fk_contributions_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

-- 11. DROP OLD INDEXES
DROP INDEX IF EXISTS idx_family_members_family;
DROP INDEX IF EXISTS idx_family_members_user;
DROP INDEX IF EXISTS idx_family_invitations_token;
DROP INDEX IF EXISTS idx_family_invitations_family;
DROP INDEX IF EXISTS idx_invitations_invited;
DROP INDEX IF EXISTS idx_invitations_family_status;
DROP INDEX IF EXISTS idx_family_contributions_family;

-- 12. CREATE NEW INDEXES
CREATE INDEX idx_goal_members_goal ON goal_members(goal_id);
CREATE INDEX idx_goal_members_user ON goal_members(user_id);
CREATE INDEX idx_goal_invitations_token ON goal_invitations(token);
CREATE INDEX idx_goal_invitations_goal ON goal_invitations(goal_id);
CREATE INDEX idx_goal_invitations_invited ON goal_invitations(invited_user_id, status);
CREATE INDEX idx_goal_invitations_goal_status ON goal_invitations(goal_id, status);
CREATE INDEX idx_goal_contributions_goal ON goal_contributions(goal_id);

-- 13. REMOVE OBSOLETE FOREIGN KEY FROM CATEGORIES (no relation with goal_units anymore)
ALTER TABLE categories DROP CONSTRAINT IF EXISTS fk_categories_family;