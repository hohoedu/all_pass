package com.hohoedu.all_pass.payment._dto;

import com.hohoedu.all_pass.student._dto.web.StudentWebRespDTO;
import lombok.Data;

import java.sql.Timestamp;
import java.util.List;

@Data
public class PaymentRespDTO {

    @Data
    public static class AssignStudentsDTO {
        private String studentId;
        private String studentName;
        private String subject;
        private String hanTeacher;
        private String bookTeacher;
        private Integer hanFee;
        private Integer hanMaterialFee;
        private Integer bookFee;
        private Integer bookMaterialFee;
        private String status;
        private String totalPrice;
        private String totalFee;
        private String totalMaterialFee;
        private String parentPhone;

    }
}
