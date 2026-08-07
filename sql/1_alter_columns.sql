IF COL_LENGTH('erp_teacher_assign_cancel_join', 'cancel_type') IS NULL
    BEGIN
        ALTER TABLE erp_teacher_assign_cancel_join
            ADD cancel_type       VARCHAR(10)  NULL,
                cancel_class_type CHAR(1)      NULL,
                cancel_user_code  NVARCHAR(50) NULL;
    END
