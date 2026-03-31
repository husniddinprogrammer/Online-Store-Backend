-- V3: Add created_at to notifications table
ALTER TABLE notifications
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW();

CREATE INDEX IF NOT EXISTS idx_notifications_created_at
    ON notifications (created_at DESC)
    WHERE is_deleted = false;
