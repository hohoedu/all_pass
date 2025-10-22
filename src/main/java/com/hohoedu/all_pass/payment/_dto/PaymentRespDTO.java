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
    }
}
