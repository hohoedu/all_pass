package com.hohoedu.all_pass.admin.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Entity
@Getter
@Table(name = "erp_book_suggest")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BookSuggest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "book_name", nullable = false)
    private String bookName;

    @Column(name = "publisher", nullable = false)
    private String publisher;

    @Column(name = "book_image_url", nullable = false)
    private String bookImageUrl;


    @Column(name = "class_key", nullable = false)
    private String classKey;

    @Column
    private String yy;

    @Column
    private String mm;

    @Column
    private String week;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_key", referencedColumnName = "subject_key", nullable = false)
    private SubjectCode subjectKey;

    @Column
    private Timestamp createdAt;

    @Column
    private Timestamp updatedAt;

    @Builder
    public BookSuggest(String bookName, String publisher, String bookImageUrl, SubjectCode subjectKey, String classKey, String yy, String mm, String week) {
        this.bookName = bookName;
        this.publisher = publisher;
        this.bookImageUrl = bookImageUrl;
        this.subjectKey = subjectKey;
        this.classKey = classKey;
        this.yy = yy;
        this.mm = mm;
        this.week = week;
    }
}
