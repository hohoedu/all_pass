package com.hohoedu.all_pass.payment._dto.app;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class PaymentAppRespDTO {

    @Data
    public static class PaymentDetailRespDTO {
        private String inym;
        private String gb;
        private String indate;
        private String inmoney;
        private String state;
        private String gubun;

        // inym => 수업 년월
        // gb => 수업 종류 (한/북)
        // indate => 결제 날짜
        // inmoney => 결제 금액
        // gubun  => 청구 종류 (교육비 / 교재비)
    }

    @Data
    public static class StudentDTO {
        private String studentId;
        private String studentName;
        private String paymentKey;
        private String phone;
        private boolean subHoho;
        private boolean subHan;
        private boolean subBook;
        private List<String> subjects;
        private List<String> siblings;
        private int tuition;
        private int arrears;
        private int textbookFee;
        private boolean textbookPaid;
        private List<StudentDTO> siblingDetails = new ArrayList<>();
    }
}
