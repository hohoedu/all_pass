package com.hohoedu.all_pass.manage._dto;

import com.hohoedu.all_pass.class_instance.model.ClassCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

@Data
public class ManageRespDTO {
    @Data
    public static class BasicOrderListDTO {
        private String className;
        private String classKey;
        private String unitName;
        private String unitKey;
        private String baseCount;
    }

    @Data
    public static class BaseOrderListDTO {
        private String className;
        private String classKey;
        private String unitName;
        private String unitKey;
        private Integer baseCount;
    }


    @Data
    public static class SavedOrderListDTO {
        private Integer baseCount;
        private Integer addCount;
        private Integer totalCount;
        private String className;
        private String classKey;
        private String unitName;
        private String unitKey;
        private String yy;
        private String mm;
    }

    @Data
    public static class ReorderListDTO {
        private Integer id;
        private String reorderType;
        private String classKey;
        private String className;
        private String unitKey;
        private String unitName;
        private String reason;
        private Integer count;
        private String confirmed;
        private String createdAt;

    }

    @Data
    @Builder
    public static class TuitionRespDTO {
        private List<ClassCode> hanClasses;
        private List<ClassCode> bookClasses;
        private List<ClassCode> hohoClasses;
        private Map<String, String> hanFeeMap;
        private Map<String, String> bookFeeMap;
        private Map<String, String> hohoFeeMap;
    }

}
