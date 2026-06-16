package com.hohoedu.all_pass.logistics._dto;

import lombok.Data;
import lombok.Getter;

import java.util.List;

@Data
public class LogisReqDTO {

    @Data
    public static class DeadlineUpdateReqDTO {
        private String centerCode;
        private int deadlineAt;
    }

    @Data
    public static class ReorderListReqDTO {
        private String year;
        private String month;
        private String centerCode;
        private boolean onlyWait;
    }

    @Data
    public static class AggregateReqDTO {
        private String year;
        private String month;
        private String segmentType;       // "전체 집계" | "센터별 집계" | "센터별 선생님 집계"
        private List<String> centerCodes;
    }

    @Data
    public static class ConfirmedUpdateReqDTO {
        private String id;
        private String confirmed;
        private String centerCode;
    }
}
