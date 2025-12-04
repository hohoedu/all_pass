package com.hohoedu.all_pass.admin.ebook._dto;

import lombok.Data;

@Data
public class EbookRespDTO {

    @Data
    public static class PersonYearDTO {
        private String centerCode;
        private String yy;
        private String mm;
        private String classKey;
        private String unitKey;
        private String subUnitKey;
    }
}
