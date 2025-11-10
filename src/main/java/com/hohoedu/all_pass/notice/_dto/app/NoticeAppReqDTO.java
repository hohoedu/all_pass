package com.hohoedu.all_pass.notice._dto.app;

import lombok.Data;

@Data
public class NoticeAppReqDTO {

    @Data
    public static class NoticeAppListReqDTO {
        private String studentId;
        private String snum;
        private Integer count;
    }
    
}
