-- Migration to create the system configuration table.
--
-- We enforce a single-row constraint on the table using CHECK (id = 1).
-- This ensures that only one config set exists, acting like a global system settings store.
--
-- min_android_version holds the minimum allowed versionCode for the Android client.
-- maintenance_mode determines if the backend is undergoing maintenance.
-- android_update_url is the target Play Store / distribution URL.

CREATE TABLE system_config (
                               id                    INT           PRIMARY KEY DEFAULT 1,
                               min_android_version   INT           NOT NULL DEFAULT 1,
                               maintenance_mode      BOOLEAN       NOT NULL DEFAULT FALSE,
                               android_update_url    TEXT          NOT NULL,
                               updated_at            TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
                               CONSTRAINT system_config_single_row CHECK (id = 1)
);

-- Seed initial configuration values
INSERT INTO system_config (id, min_android_version, maintenance_mode, android_update_url)
VALUES (1, 12, false, 'https://play.google.com/store/apps/details?id=com.adel.wc26')
    ON CONFLICT (id) DO NOTHING;