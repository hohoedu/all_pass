package com.hohoedu.all_pass.notice;

import com.hohoedu.all_pass.center.Center;
import com.hohoedu.all_pass.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.checkerframework.checker.fenum.qual.Fenum;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Fetch;

import java.sql.Timestamp;

@Entity
@Getter
@Table(name = "erp_center_notice", uniqueConstraints = @UniqueConstraint(name = "uk_center_notice_key", columnNames = "center_notice_key"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CenterNotice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String subTitle;

    @Lob
    @Column(nullable = false)
    private String content;

    @Column(name = "image")
    private String image;

    @Column(name = "icon")
    private String icon;

    @Column(name = "view_count")
    private Integer viewCount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_code", referencedColumnName = "user_code")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "center_code", referencedColumnName = "center_code")
    private Center center;

    @CreationTimestamp
    private Timestamp createAt;

    @Builder
    public CenterNotice(String title, String subTitle, String content, String image, String icon, Integer viewCount, User user, Center center) {
        this.title = title;
        this.subTitle = subTitle;
        this.content = content;
        this.image = image;
        this.icon = icon;
        this.viewCount = viewCount;
        this.user = user;
        this.center = center;
    }
}
