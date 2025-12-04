package com.hohoedu.all_pass.class_instance.model;

import com.hohoedu.all_pass.admin.center.Center;
import com.hohoedu.all_pass.class_instance.TimeTable;
import com.hohoedu.all_pass.student.Student;
import com.hohoedu.all_pass.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Entity
@Table(name = "erp_infant_han_notice")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InfantHanNotice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "main_title", length = 200)
    private String mainTitle;

    @Column(name = "subtitle", length = 200)
    private String subtitle;

    @Column(name = "title1", length = 500)
    private String title1;

    @Column(name = "title2", length = 500)
    private String title2;

    @Column(name = "title3", length = 500)
    private String title3;

    @Column(name = "img_url", length = 500)
    private String img;

    @Column(name = "part1_title", length = 200)
    private String part1Title;
    @Column(name = "part2_title", length = 200)
    private String part2Title;
    @Column(name = "part3_title", length = 200)
    private String part3Title;
    @Column(name = "part4_title", length = 200)
    private String part4Title;
    @Column(name = "part5_title", length = 200)
    private String part5Title;

    @Lob
    @Column(name = "part1_note")
    private String part1Note;

    @Lob
    @Column(name = "part2_note")
    private String part2Note;

    @Lob
    @Column(name = "part3_note")
    private String part3Note;

    @Lob
    @Column(name = "part4_note")
    private String part4Note;

    @Lob
    @Column(name = "part5_note")
    private String part5Note;

    @Column(name = "part1_tag_1", length = 100)
    private String part1Tag1;
    @Column(name = "part1_tag_2", length = 100)
    private String part1Tag2;
    @Column(name = "part1_tag_3", length = 100)
    private String part1Tag3;

    @Column(name = "part2_tag_1", length = 100)
    private String part2Tag1;
    @Column(name = "part2_tag_2", length = 100)
    private String part2Tag2;
    @Column(name = "part2_tag_3", length = 100)
    private String part2Tag3;

    @Column(name = "part3_tag_1", length = 100)
    private String part3Tag1;
    @Column(name = "part3_tag_2", length = 100)
    private String part3Tag2;
    @Column(name = "part3_tag_3", length = 100)
    private String part3Tag3;

    @Column(name = "part4_tag_1", length = 100)
    private String part4Tag1;
    @Column(name = "part4_tag_2", length = 100)
    private String part4Tag2;
    @Column(name = "part4_tag_3", length = 100)
    private String part4Tag3;

    @Column(name = "part5_tag_1", length = 100)
    private String part5Tag1;
    @Column(name = "part5_tag_2", length = 100)
    private String part5Tag2;
    @Column(name = "part5_tag_3", length = 100)
    private String part5Tag3;

    // ===== 감사 정보 ===== //
    @Column(name = "created_at", updatable = false)
    private Timestamp createdAt;

    @Column(name = "updated_at")
    private Timestamp updatedAt;

    @Column(name = "update_user", length = 20)
    private String updateUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", referencedColumnName = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "time_table_key", referencedColumnName = "time_table_key", nullable = false)
    private TimeTable timeTable;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "center_code", referencedColumnName = "center_code", nullable = false)
    private Center center;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_code", referencedColumnName = "user_code", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "send_id", referencedColumnName = "send_id")
    private InfantSendHistory sendHistory;

    @Builder
    public InfantHanNotice(String mainTitle, String subtitle, String title1, String title2, String title3, String img, String part1Title, String part2Title, String part3Title, String part4Title, String part5Title, String part1Note, String part2Note, String part3Note, String part4Note, String part5Note, String part1Tag1, String part1Tag2, String part1Tag3, String part2Tag1, String part2Tag2, String part2Tag3, String part3Tag1, String part3Tag2, String part3Tag3, String part4Tag1, String part4Tag2, String part4Tag3, String part5Tag1, String part5Tag2, String part5Tag3, Timestamp createdAt, Timestamp updatedAt, String updateUser, Student student, TimeTable timeTable, Center center, User user, InfantSendHistory sendHistory) {
        this.mainTitle = mainTitle;
        this.subtitle = subtitle;
        this.title1 = title1;
        this.title2 = title2;
        this.title3 = title3;
        this.img = img;
        this.part1Title = part1Title;
        this.part2Title = part2Title;
        this.part3Title = part3Title;
        this.part4Title = part4Title;
        this.part5Title = part5Title;
        this.part1Note = part1Note;
        this.part2Note = part2Note;
        this.part3Note = part3Note;
        this.part4Note = part4Note;
        this.part5Note = part5Note;
        this.part1Tag1 = part1Tag1;
        this.part1Tag2 = part1Tag2;
        this.part1Tag3 = part1Tag3;
        this.part2Tag1 = part2Tag1;
        this.part2Tag2 = part2Tag2;
        this.part2Tag3 = part2Tag3;
        this.part3Tag1 = part3Tag1;
        this.part3Tag2 = part3Tag2;
        this.part3Tag3 = part3Tag3;
        this.part4Tag1 = part4Tag1;
        this.part4Tag2 = part4Tag2;
        this.part4Tag3 = part4Tag3;
        this.part5Tag1 = part5Tag1;
        this.part5Tag2 = part5Tag2;
        this.part5Tag3 = part5Tag3;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.updateUser = updateUser;
        this.student = student;
        this.timeTable = timeTable;
        this.center = center;
        this.user = user;
        this.sendHistory = sendHistory;
    }
}
