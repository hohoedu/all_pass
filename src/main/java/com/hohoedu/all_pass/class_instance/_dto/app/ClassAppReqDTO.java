package com.hohoedu.all_pass.class_instance._dto.app;

import lombok.Data;

@Data
public class ClassAppReqDTO {
    @Data
    public static class ClassInfoReqDTO {
        private String id;
        private String yyyy;
        private String mm;
    }
}
