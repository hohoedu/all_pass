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

    @Data
    public static class CenterStatusDTO {
        private String centerCode;
        private String centerName;
        private int studentCount; // 재원 (해당 월 시간표 배정 학생)
        private int newCount; // 신규 (해당 월 입회)
        private int leaveCount; // 퇴원 (해당 월 탈퇴 로그)
        private int unpaidCount; // 교재비 미납 학생 수
        private int unpaidAmount; // 교재비 미납 금액
        private double unpaidRate; // 미납률 (미납 / 재원 * 100)
    }

    @Data
    public static class UnpaidBookFeeDTO {
        private String centerCode;
        private String centerName;
        private String studentId;
        private String studentName;
        private String gradeName;
        private String teacherName;
        private String bookName;
        private int amount;
        private String ym;
        private String status; // unbilled: 미청구, issued: 청구중, expired: 기한만료/취소
    }

}
