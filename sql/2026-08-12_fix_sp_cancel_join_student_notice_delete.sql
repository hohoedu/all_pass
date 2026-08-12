-- sp_cancel_join_student 버그 수정
-- 문제: 과목별 취소(classType='1'/'2')로 시작해 두 과목이 모두 해제되어
--       전체삭제(doFullDelete=1)로 넘어가는 경로에서는 erp_before_class_notice /
--       erp_after_class_notice 를 지우지 않아, 다른 과목분 notice가 남은 채로
--       DELETE FROM erp_student를 실행 → FK(FKms79cjss2aakp9a0jvjwdh0u9) 위반 발생.
-- 조치: 전체삭제 블록에 두 notice 테이블 DELETE를 추가 (다른 테이블들과 동일하게 무조건 삭제).

IF OBJECT_ID('sp_cancel_join_student', 'P') IS NOT NULL
    DROP PROCEDURE sp_cancel_join_student;

CREATE PROCEDURE sp_cancel_join_student
    @studentId NVARCHAR(50),
    @classType CHAR(1)      = NULL,
    @userCode  NVARCHAR(50) = NULL,
    @isAdmin   BIT          = 0
AS
BEGIN
    SET NOCOUNT ON;

    DECLARE @doFullDelete BIT = CASE WHEN @classType IS NULL THEN 1 ELSE 0 END;

    BEGIN TRANSACTION;
    BEGIN TRY

        IF NOT EXISTS (SELECT 1 FROM erp_student WHERE student_id = @studentId)
            BEGIN
                THROW 50001, '존재하지 않는 학생입니다.', 1;
            END

        IF EXISTS (SELECT 1 FROM erp_payment_bill WHERE student_id = @studentId AND status = 'approved')
            BEGIN
                THROW 50002, '결제 이력이 있습니다. 결제를 먼저 취소해주세요.', 1;
            END

        IF @classType IS NOT NULL
            BEGIN

                IF @classType NOT IN ('1', '2')
                    BEGIN
                        THROW 50008, '잘못된 과목 구분입니다.', 1;
                    END

                DECLARE @entryDate DATE = (
                    SELECT CASE WHEN @classType = '1' THEN TRY_CAST(entry_han_date AS DATE)
                                ELSE TRY_CAST(entry_book_date AS DATE) END
                    FROM erp_teacher_assign
                    WHERE student_id = @studentId);

                IF @entryDate IS NULL
                    BEGIN
                        THROW 50005, '해당 과목의 입회 내역이 없습니다.', 1;
                    END

                IF EXISTS (SELECT 1
                           FROM erp_teacher_assign
                           WHERE student_id = @studentId
                             AND ((@classType = '1' AND inactive_han_date IS NOT NULL)
                               OR (@classType = '2' AND inactive_book_date IS NOT NULL)))
                    BEGIN
                        THROW 50007, '이미 탈퇴 처리된 과목은 입회 취소가 불가합니다.', 1;
                    END

                IF EXISTS (SELECT 1
                           FROM erp_student_withdraw_log
                           WHERE student_id = @studentId
                             AND log_type = CASE @classType WHEN '1' THEN 'han' ELSE 'book' END)
                    BEGIN
                        THROW 50007, '탈퇴 이력이 있어 입회 취소가 불가합니다.', 1;
                    END

                IF @isAdmin = 0
                    AND NOT EXISTS (SELECT 1
                                    FROM erp_teacher_assign
                                    WHERE student_id = @studentId
                                      AND ((@classType = '1' AND assign_han_teacher = @userCode)
                                        OR (@classType = '2' AND assign_book_teacher = @userCode)))
                    BEGIN
                        THROW 50006, '담당 선생님만 입회 취소가 가능합니다.', 1;
                    END

                IF EXISTS (SELECT 1
                           FROM erp_before_class_notice
                           WHERE student_id = @studentId
                             AND class_type = @classType
                             AND TRY_CAST(class_date AS DATE) > DATEADD(DAY, 7, @entryDate)
                           UNION ALL
                           SELECT 1
                           FROM erp_student_attendance sa
                                    JOIN erp_time_table tt ON tt.time_table_key = sa.time_table_key
                                    JOIN erp_class_code cc ON cc.class_key = tt.class_key
                           WHERE sa.student_id = @studentId
                             AND cc.class_type = @classType
                             AND TRY_CAST(sa.attendance_date AS DATE) > DATEADD(DAY, 7, @entryDate))
                    BEGIN
                        THROW 50003, '입회 첫 주 이후 수업 이력이 있어 입회 취소가 불가합니다.', 1;
                    END

                INSERT INTO erp_teacher_assign_cancel_join (han_state, book_state, han_material_fee, book_material_fee,
                                                            assign_han_class, assign_book_class,
                                                            assign_han_teacher, assign_book_teacher,
                                                            entry_han_date, entry_book_date, student_id, center_code,
                                                            inactive_han_date, inactive_book_date,
                                                            inactive_han_reason, inactive_book_reason,
                                                            inactive_han_teacher, inactive_book_teacher,
                                                            created_at, updated_at, deleted_at,
                                                            cancel_type, cancel_class_type, cancel_user_code)
                SELECT han_state, book_state, han_material_fee, book_material_fee,
                       assign_han_class, assign_book_class,
                       assign_han_teacher, assign_book_teacher,
                       entry_han_date, entry_book_date, student_id, center_code,
                       inactive_han_date, inactive_book_date,
                       inactive_han_reason, inactive_book_reason,
                       inactive_han_teacher, inactive_book_teacher,
                       created_at, updated_at, GETDATE(),
                       'SUBJECT', @classType, @userCode
                FROM erp_teacher_assign
                WHERE student_id = @studentId;

                DECLARE @ttKeys TABLE (time_table_key NVARCHAR(50) PRIMARY KEY);
                INSERT INTO @ttKeys (time_table_key)
                SELECT tt.time_table_key
                FROM erp_time_table tt
                         JOIN erp_class_code cc ON cc.class_key = tt.class_key
                WHERE cc.class_type = @classType;

                DELETE FROM erp_before_class_notice
                WHERE student_id = @studentId AND class_type = @classType;

                DELETE FROM erp_after_class_notice
                WHERE student_id = @studentId AND class_type = @classType;

                DELETE FROM erp_remedial
                WHERE student_id = @studentId
                  AND time_table_key IN (SELECT time_table_key FROM @ttKeys);

                DELETE FROM erp_student_attendance
                WHERE student_id = @studentId
                  AND time_table_key IN (SELECT time_table_key FROM @ttKeys);

                DELETE FROM erp_monthly_score
                WHERE student_id = @studentId
                  AND time_table_key IN (SELECT time_table_key FROM @ttKeys);

                DELETE FROM erp_student_remark
                WHERE student_id = @studentId
                  AND time_table_key IN (SELECT time_table_key FROM @ttKeys);

                DELETE FROM erp_time_table_assign
                WHERE student_id = @studentId
                  AND time_table_key IN (SELECT time_table_key FROM @ttKeys);

                DELETE pd
                FROM erp_payment_detail pd
                         JOIN erp_payment p ON p.payment_key = pd.payment_key
                WHERE p.student_id = @studentId
                  AND pd.class_type = @classType;

                UPDATE p
                SET amount        = ISNULL(d.total, 0),
                    unpaid_amount = ISNULL(d.total, 0)
                FROM erp_payment p
                         OUTER APPLY (SELECT SUM(amount) AS total
                                      FROM erp_payment_detail
                                      WHERE payment_key = p.payment_key) d
                WHERE p.student_id = @studentId;

                IF @classType = '1'
                    UPDATE erp_teacher_assign
                    SET han_state          = NULL,
                        assign_han_class   = NULL,
                        assign_han_teacher = NULL,
                        entry_han_date     = NULL,
                        han_material_fee   = NULL,
                        updated_at         = SYSDATETIME()
                    WHERE student_id = @studentId;
                ELSE
                    UPDATE erp_teacher_assign
                    SET book_state          = NULL,
                        assign_book_class   = NULL,
                        assign_book_teacher = NULL,
                        entry_book_date     = NULL,
                        book_material_fee   = NULL,
                        updated_at          = SYSDATETIME()
                    WHERE student_id = @studentId;

                IF NOT EXISTS (SELECT 1
                               FROM erp_teacher_assign
                               WHERE student_id = @studentId
                                 AND (entry_han_date IS NOT NULL OR inactive_han_date IS NOT NULL
                                   OR entry_book_date IS NOT NULL OR inactive_book_date IS NOT NULL))
                    BEGIN
                        SET @doFullDelete = 1;
                    END
            END

        IF @doFullDelete = 1
            BEGIN

                IF @classType IS NULL
                    BEGIN
                        IF EXISTS (SELECT 1 FROM erp_after_class_notice WHERE student_id = @studentId)
                            BEGIN
                                THROW 50003, '수업 이력이 존재하는 학생은 입회 취소가 불가합니다.', 1;
                            END

                        IF EXISTS (SELECT 1 FROM erp_before_class_notice WHERE student_id = @studentId)
                            BEGIN
                                THROW 50004, '수업 이력이 존재하는 학생은 입회 취소가 불가합니다.', 1;
                            END
                    END

                INSERT INTO erp_student_cancel_join (gender, student_privacy_agree, created_at, updated_at,
                                                     app_id, center_code, grade_key, status_key, school,
                                                     student_id, student_name, address, address_detail,
                                                     app_password, app_token, birth, profile_img, consult_key,
                                                     is_hoho, billing_phone, sub_han, sub_book, sub_hoho,
                                                     deleted_at)
                SELECT gender, student_privacy_agree, created_at, updated_at,
                       app_id, center_code, grade_key, status_key, school,
                       student_id, student_name, address, address_detail,
                       app_password, app_token, birth, profile_img, consult_key,
                       is_hoho, billing_phone, sub_han, sub_book, sub_hoho,
                       GETDATE()
                FROM erp_student
                WHERE student_id = @studentId;

                INSERT INTO erp_parent_cancel_join (parent_privacy_agree, created_at, updated_at,
                                                    parent_tel_first, parent_tel_last, parent_tel_middle,
                                                    parent_name, relation_key, student_id, signature,
                                                    deleted_at)
                SELECT parent_privacy_agree, created_at, updated_at,
                       parent_tel_first, parent_tel_last, parent_tel_middle,
                       parent_name, relation_key, student_id, signature,
                       GETDATE()
                FROM erp_parent
                WHERE student_id = @studentId;

                INSERT INTO erp_teacher_assign_cancel_join (han_state, book_state, han_material_fee, book_material_fee,
                                                            assign_han_class, assign_book_class,
                                                            assign_han_teacher, assign_book_teacher,
                                                            entry_han_date, entry_book_date, student_id, center_code,
                                                            inactive_han_date, inactive_book_date,
                                                            inactive_han_reason, inactive_book_reason,
                                                            inactive_han_teacher, inactive_book_teacher,
                                                            created_at, updated_at, deleted_at,
                                                            cancel_type, cancel_class_type, cancel_user_code)
                SELECT han_state, book_state, han_material_fee, book_material_fee,
                       assign_han_class, assign_book_class,
                       assign_han_teacher, assign_book_teacher,
                       entry_han_date, entry_book_date, student_id, center_code,
                       inactive_han_date, inactive_book_date,
                       inactive_han_reason, inactive_book_reason,
                       inactive_han_teacher, inactive_book_teacher,
                       created_at, updated_at, GETDATE(),
                       'ALL', NULL, @userCode
                FROM erp_teacher_assign
                WHERE student_id = @studentId;

                INSERT INTO erp_time_table_assign_cancel_join (week, class_key, unit_key, student_id, time_table_key,
                                                               deleted_at)
                SELECT week, class_key, unit_key, student_id, time_table_key, GETDATE()
                FROM erp_time_table_assign
                WHERE student_id = @studentId;

                INSERT INTO erp_status_history_cancel_join (updated_at, status_key, student_id,
                                                            user_code, reason, withdraw_date, deleted_at)
                SELECT updated_at, status_key, student_id,
                       user_code, reason, withdraw_date, GETDATE()
                FROM erp_status_history
                WHERE student_id = @studentId;

                DELETE FROM erp_before_class_notice WHERE student_id = @studentId;
                DELETE FROM erp_after_class_notice WHERE student_id = @studentId;
                DELETE FROM erp_remedial WHERE student_id = @studentId;
                DELETE FROM erp_student_attendance WHERE student_id = @studentId;
                DELETE FROM erp_monthly_score WHERE student_id = @studentId;
                DELETE FROM erp_student_remark WHERE student_id = @studentId;
                DELETE FROM erp_parent WHERE student_id = @studentId;
                DELETE FROM erp_student_transfer_history WHERE student_id = @studentId;
                DELETE FROM erp_teacher_assign WHERE student_id = @studentId;
                DELETE FROM erp_time_table_assign WHERE student_id = @studentId;
                DELETE FROM erp_status_history WHERE student_id = @studentId;

                ALTER TABLE erp_payment_manual NOCHECK CONSTRAINT fk_payment_manual_student;

                UPDATE erp_payment_manual
                SET student_id = @studentId + '_del'
                WHERE student_id = @studentId;

                ALTER TABLE erp_payment_manual CHECK CONSTRAINT fk_payment_manual_student;

                ALTER TABLE erp_payment_bill NOCHECK CONSTRAINT FK6yst0lcebvslwa9ut2yrhc9nr;

                UPDATE erp_payment_bill
                SET student_id = @studentId + '_del'
                WHERE student_id = @studentId;

                ALTER TABLE erp_payment_bill CHECK CONSTRAINT FK6yst0lcebvslwa9ut2yrhc9nr;

                ALTER TABLE erp_payment NOCHECK CONSTRAINT FKdg7l3ch624auie81jrb2thq1b;

                UPDATE erp_payment
                SET student_id = @studentId + '_del'
                WHERE student_id = @studentId;

                ALTER TABLE erp_payment CHECK CONSTRAINT FKdg7l3ch624auie81jrb2thq1b;

                DELETE FROM erp_student WHERE student_id = @studentId;
            END

        COMMIT;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0
            ROLLBACK;
        THROW;
    END CATCH
END;
