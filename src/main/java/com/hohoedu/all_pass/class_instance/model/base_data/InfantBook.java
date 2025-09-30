package com.hohoedu.all_pass.class_instance.model.base_data;

import com.hohoedu.all_pass.class_instance.model.ClassCode;
import com.hohoedu.all_pass.class_instance.model.UnitCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "erp_infant_book")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InfantBook {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "yy", nullable = false)
    private String yy;

    @Column(name = "image_path")
    private String imagePath;

    @Column(name = "subject", columnDefinition = "nvarchar(300)")
    private String subject;

    @Column(name = "content", columnDefinition = "nvarchar(300)")
    private String content;

    // 위인 동화
    @Column(name = "story", columnDefinition = "nvarchar(300)")
    private String story;

    // 한글 알기
    @Column(name = "hangul", columnDefinition = "nvarchar(300)")
    private String hangul;

    // 워크북
    @Column(name = "workbook", columnDefinition = "nvarchar(300)")
    private String workbook;

    // 지식 활동 보드
    @Column(name = "knowledge_board", columnDefinition = "nvarchar(300)")
    private String knowledgeBoard;

    // 동요
    @Column(name = "song", columnDefinition = "nvarchar(300)")
    private String song;

    // 생각말하기
    @Column(name = "think_talk", columnDefinition = "nvarchar(300)")
    private String thinkTalk;

    // 공감 독서
    @Column(name = "empathy", columnDefinition = "nvarchar(300)")
    private String empathy;

    // 골든벨
    @Column(name = "goldenbell", columnDefinition = "nvarchar(300)")
    private String goldenbell;

    // 다른그림 찾기
    @Column(name = "find_diff", columnDefinition = "nvarchar(300)")
    private String findDiff;

    // 동화 꾸미기
    @Column(name = "make_story", columnDefinition = "nvarchar(300)")
    private String makeStory;

    // 그림맞추기
    @Column(name = "pic_match", columnDefinition = "nvarchar(300)")
    private String picMatch;

    @Column(name = "note", columnDefinition = "nvarchar(300)")
    private String note;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_key", referencedColumnName = "class_key", nullable = false)
    private ClassCode classCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_key", referencedColumnName = "unit_key", nullable = false)
    private UnitCode unitCode;

    @Builder
    public InfantBook(String yy, String imagePath, String subject, String content, String story, String hangul, String workbook, String knowledgeBoard, String song, String thinkTalk, String empathy, String goldenbell, String findDiff, String makeStory, String picMatch, String note, ClassCode classCode, UnitCode unitCode) {
        this.yy = yy;
        this.imagePath = imagePath;
        this.subject = subject;
        this.content = content;
        this.story = story;
        this.hangul = hangul;
        this.workbook = workbook;
        this.knowledgeBoard = knowledgeBoard;
        this.song = song;
        this.thinkTalk = thinkTalk;
        this.empathy = empathy;
        this.goldenbell = goldenbell;
        this.findDiff = findDiff;
        this.makeStory = makeStory;
        this.picMatch = picMatch;
        this.note = note;
        this.classCode = classCode;
        this.unitCode = unitCode;
    }
}
