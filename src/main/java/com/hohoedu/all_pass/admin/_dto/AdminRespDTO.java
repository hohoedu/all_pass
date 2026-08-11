package com.hohoedu.all_pass.admin._dto;

import lombok.Data;

@Data
public class AdminRespDTO {

    @Data
    public static class PersonYearDTO {
        private String centerCode;
        private String yy;
        private String mm;
        private String classKey;
        private String unitKey;
        private String subUnitKey;
    }

    @Data
    public static class KeycodeDTO {
        private String classKey; // 교재 코드
        private String className; // 교재명
        private String classType; // 교재 타입 (1: 한자, 2: 독서)
        private String unitKey; // 호수 코드
        private String unitName; // 호수명
        private String ocode; // 이북 코드 접두어 (erp_secondary_class_map.ocode)
        private boolean levelUnit; // 급수 여부 (급수는 이북 코드를 만들지 않는다)
        private String ggubun; // 외부 DB 교재 코드 (유곡점만)
        private String mgubun; // 외부 DB 호수 코드 (유곡점만)
        private String keyCode; // 이미 생성된 이북 코드 (third DB, 없으면 화면에서 생성)
        private String settingValue; // 이북 코드 접두어 둘째 자리에서 읽어낸 세팅값 (X/Y/Z/A)
    }

    @Data
    public static class BookSuggestViewDTO {
        private String classKey;
        private String yy;
        private String mm;
        private String week;
        private String subjectKey;
        private String bookName;
        private String publisher;
        private String bookImageUrl;
    }

}
