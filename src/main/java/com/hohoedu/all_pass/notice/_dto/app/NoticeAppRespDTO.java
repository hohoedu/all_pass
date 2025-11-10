package com.hohoedu.all_pass.notice._dto.app;

import lombok.Data;

@Data
public class NoticeAppRespDTO {

    @Data
    public static class NoticeListRespDTO {
        private String idx;
        private String noticeKey;
        private String title;
        private String subtitle;
        private String content;
        private String subicon;
        private String sdate;
    }

    @Data
    public static class NoticeDetailRespDTO {
        private String idx;
        private String noticeKey;
        private String title;
        private String subtitle;
        private String note;
        private String subicon;
        private String imagepath;
        private String linkurl;
    }

}
