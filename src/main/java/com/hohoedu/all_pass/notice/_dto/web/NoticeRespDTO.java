package com.hohoedu.all_pass.notice._dto.web;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class NoticeRespDTO {

    @Data
    public static class CenterNoticeDTO {
        private Integer id;
        private String centerNoticeKey;
        private String title;
        private String subTitle;
        private String rawContent;
        private String cleanContent;
        private String icon;
        private String url;
        private Integer viewCount;
        private Timestamp createdAt;
        private String userName;

        public static String sanitizeHtml(String html) {
            if (html == null) return "";
            return html.replaceAll("<[^>]*>", " ");
        }

    }


    @Data
    public static class CenterNoticeDetailDTO {
        private Integer id;
        private String centerNoticeKey;
        private String title;
        private String subTitle;
        private String rawContent;
        private String cleanContent;
        private String icon;
        private String url;
        private Integer viewCount;
        private Timestamp createdAt;
        private String userName;


        public static String sanitizeHtml(String html) {
            if (html == null) return "";
            // 허용할 태그만 직접 지정 가능 (단순 예시)
            return html.replaceAll("<[^>]*>", " ");
        }
    }

    @Data
    public static class NoticeStudentDTO {
        private String studentId;
        private String studentName;
        private String appToken;
        private String subject;
        private String grade;
    }
}
