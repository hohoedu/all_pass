package com.hohoedu.all_pass.notice._dto.web;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
public class NoticeReqDTO {

    @Data
    public static class CenterNoticeSaveReqDTO {
        private String centerNoticeKey;
        private String title;
        private String subTitle;
        private String content;
        private String icon;
        private String linkUrl;
        private String image;
        private Integer viewCount;
        private String userCode;
        private String centerCode;
        private List<String> tokens;
        private List<String> studentIds;
    }

    @Data
    public static class CenterNoticeDeleteDTO {
        private Integer id;
    }

    @Data
    public static class  CenterNoticeUpdateDTO {
        private Integer id;           // 수정할 공지 ID (필수)
        private String title;         // 제목
        private String subTitle;      // 부제목 (sub_title)
        private Integer icon;         // 아이콘 번호
        private String content;       // 내용
        private String linkUrl;       // 링크 URL (link_url)
        private String image;         // 이미지 경로
        private String centerNoticeKey; // 알림 재발송용 공지 key
        private List<String> tokens;    // 새로 추가된 학생의 앱 토큰
        private List<String> studentIds; // 새로 추가된 학생 id
    }

    @Data
    @Builder
    public static class  RemedialNoticeDTO {
        private String centerCode;
        private String centerNoticeKey;
        private String title;
        private String subTitle;
        private String content;
        private String icon;
        private String linkUrl;
        private String image;
        private Integer viewCount;
        private String userCode;
    }

}
