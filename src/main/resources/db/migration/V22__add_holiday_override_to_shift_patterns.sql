-- Add holiday override fields to shift_patterns table
-- These fields allow shift to work on holidays

ALTER TABLE shift_patterns
    ADD COLUMN override_national_holiday BOOLEAN DEFAULT FALSE
        COMMENT 'Jika TRUE, shift ini tetap kerja di hari libur nasional' AFTER display_order,

    ADD COLUMN override_company_holiday BOOLEAN DEFAULT FALSE
        COMMENT 'Jika TRUE, shift ini tetap kerja di hari libur perusahaan' AFTER override_national_holiday,

    ADD COLUMN override_joint_leave BOOLEAN DEFAULT FALSE
        COMMENT 'Jika TRUE, shift ini tetap kerja di cuti bersama' AFTER override_company_holiday,

    ADD COLUMN override_weekly_leave BOOLEAN DEFAULT FALSE
        COMMENT 'Jika TRUE, shift ini kerja di hari minggu/libur mingguan' AFTER override_joint_leave;
