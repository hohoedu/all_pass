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
@Table(name = "erp_infant_han")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InfantHan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "yy", nullable = false)
    private String yy;

    @Column(name = "image_path")
    private String imagePath;

    @Column(name = "new_word", columnDefinition = "nvarchar(300)")
    private String newWord;

    @Column(name = "story", columnDefinition = "nvarchar(300)")
    private String story;

    @Column(name = "sub_story", columnDefinition = "nvarchar(300)")
    private String subStory;

    @Column(name = "story_comment", columnDefinition = "nvarchar(300)")
    private String storyComment;

    @Column(name = "idiom", columnDefinition = "nvarchar(300)")
    private String idiom;

    @Column(name = "sub_idiom", columnDefinition = "nvarchar(300)")
    private String sudIdiom;

    @Column(name = "workbook", columnDefinition = "nvarchar(300)")
    private String workbook;

    @Column(name = "hangul_playground", columnDefinition = "nvarchar(300)")
    private String hangulPlayground;

    @Column(name = "resource_song", columnDefinition = "nvarchar(300)")
    private String resourceSong;

    @Column(name = "insung", columnDefinition = "nvarchar(300)")
    private String insung;

    @Column(name = "card", columnDefinition = "nvarchar(300)")
    private String card;

    @Column(name = "drawing", columnDefinition = "nvarchar(300)")
    private String drawing;

    @Column(name = "clean", columnDefinition = "nvarchar(300)")
    private String clean;

    @Column(name = "hanja_song", columnDefinition = "nvarchar(300)")
    private String hanjaSong;

    @Column(name = "promise", columnDefinition = "nvarchar(300)")
    private String promise;

    @Column(name = "flip", columnDefinition = "nvarchar(300)")
    private String flip;

    @Column(name = "puzzle", columnDefinition = "nvarchar(300)")
    private String puzzle;

    @Column(name = "goldenbell", columnDefinition = "nvarchar(300)")
    private String goldenbell;

    @Column(name = "subject", columnDefinition = "nvarchar(300)")
    private String subject;

    @Column(name = "note", columnDefinition = "nvarchar(300)")
    private String note;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_key", referencedColumnName = "class_key", nullable = false)
    private ClassCode classCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_key", referencedColumnName = "unit_key", nullable = false)
    private UnitCode unitCode;

    @Builder
    public InfantHan(String yy, String imagePath, String newWord, String story, String subStory, String storyComment, String idiom, String sudIdiom, String workbook, String hangulPlayground, String resourceSong, String insung, String card, String drawing, String clean, String hanjaSong, String promise, String flip, String puzzle, String goldenbell, String subject, String note, ClassCode classCode, UnitCode unitCode) {
        this.yy = yy;
        this.imagePath = imagePath;
        this.newWord = newWord;
        this.story = story;
        this.subStory = subStory;
        this.storyComment = storyComment;
        this.idiom = idiom;
        this.sudIdiom = sudIdiom;
        this.workbook = workbook;
        this.hangulPlayground = hangulPlayground;
        this.resourceSong = resourceSong;
        this.insung = insung;
        this.card = card;
        this.drawing = drawing;
        this.clean = clean;
        this.hanjaSong = hanjaSong;
        this.promise = promise;
        this.flip = flip;
        this.puzzle = puzzle;
        this.goldenbell = goldenbell;
        this.subject = subject;
        this.note = note;
        this.classCode = classCode;
        this.unitCode = unitCode;
    }
}
