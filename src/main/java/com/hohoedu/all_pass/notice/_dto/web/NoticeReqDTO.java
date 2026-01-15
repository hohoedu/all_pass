package com.hohoedu.all_pass.notice._dto.web;

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
    }

}
