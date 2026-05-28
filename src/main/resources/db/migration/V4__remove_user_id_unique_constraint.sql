-- Remove unique constraint on user_id to allow multiple subscriptions per user
-- This allows users to upgrade, renew, or have multiple subscription records over time

ALTER TABLE subscriptions DROP CONSTRAINT IF EXISTS ukl3ommhd1n0tu0k2va0cbp87qe;
ALTER TABLE subscriptions DROP CONSTRAINT IF EXISTS uk_user_id;

-- Add index for faster lookups (non-unique)
CREATE INDEX IF NOT EXISTS idx_subscriptions_user_id ON subscriptions(user_id);
CREATE INDEX IF NOT EXISTS idx_subscriptions_status ON subscriptions(status);
