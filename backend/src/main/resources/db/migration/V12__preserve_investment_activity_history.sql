UPDATE activities
SET active = TRUE
WHERE active = FALSE;

ALTER TABLE activities
    ADD CONSTRAINT chk_investment_activities_remain_active
    CHECK (active = TRUE);
