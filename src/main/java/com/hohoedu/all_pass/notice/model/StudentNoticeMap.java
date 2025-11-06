package com.hohoedu.all_pass.notice.model;

import com.hohoedu.all_pass.notice.CenterNotice;
import com.hohoedu.all_pass.student.Student;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "erp_student_notice_map", uniqueConstraints = @UniqueConstraint(name = "uk_notice_student", columnNames = {"notice_id", "student_id"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StudentNoticeMap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "send_yn", nullable = false)
    private boolean sendYn = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false, referencedColumnName = "student_id")
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notice_id", nullable = false)
    private CenterNotice centerNotice;

    @Builder
    public StudentNoticeMap(boolean sendYn, Student student, CenterNotice centerNotice) {
        this.sendYn = sendYn;
        this.student = student;
        this.centerNotice = centerNotice;
    }
}
