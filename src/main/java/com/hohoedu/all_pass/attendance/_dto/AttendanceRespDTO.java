package com.hohoedu.all_pass.attendance._dto;

import com.hohoedu.all_pass.class_instance.TimeTable;
import lombok.*;

import java.util.List;

@Data
public class AttendanceRespDTO {
    @Data
    public static class ScheduleRunResultDTO {
        private int processedCount;               // 처리 완료된 수업 수
        private int skippedCount;                 // 스킵된 수업 수
        private List<ProcessedClassDTO> details;  // 개별 수업 처리 결과
    }

    @Data
    @Builder
    public static class ProcessedClassDTO {
        private String timeTableKey;
        private String className;
        private String period;
        private String status;       // PROCESSED | SKIPPED | FAILED
        private String errorMessage; // 실패 시만 채움

        public static ProcessedClassDTO processedOf(TimeTable tt) {
            return ProcessedClassDTO.builder()
                    .timeTableKey(tt.getTimeTableKey())
                    .className(tt.getClassCode() != null ? tt.getClassCode().getClassName() : null)
                    .period(tt.getStartTime() + "~" + tt.getEndTime())
                    .status("PROCESSED")
                    .build();
        }

        public static ProcessedClassDTO skippedOf(TimeTable tt) {
            return ProcessedClassDTO.builder()
                    .timeTableKey(tt.getTimeTableKey())
                    .className(tt.getClassCode() != null ? tt.getClassCode().getClassName() : null)
                    .period(tt.getStartTime() + "~" + tt.getEndTime())
                    .status("SKIPPED")
                    .build();
        }

        public static ProcessedClassDTO failedOf(TimeTable tt, String errorMsg) {
            return ProcessedClassDTO.builder()
                    .timeTableKey(tt.getTimeTableKey())
                    .className(tt.getClassCode() != null ? tt.getClassCode().getClassName() : null)
                    .period(tt.getStartTime() + "~" + tt.getEndTime())
                    .status("FAILED")
                    .errorMessage(errorMsg)
                    .build();
        }
    }
}
