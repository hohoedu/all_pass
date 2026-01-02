package com.hohoedu.all_pass._core.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.jsonwebtoken.lang.Collections;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.List;

public final class AppApiUtils {

    private AppApiUtils() {
    }

    public static final String RESULT_OK = "0000";

    public static <T> ApiEnvelope<T> successOne(T item) {
        List<T> list = (item == null) ? Collections.emptyList() : List.of(item);
        return new ApiEnvelope<>("true", RESULT_OK, list, "null");
    }

    public static <T> ApiEnvelope<T> successList(List<T> items) {
        List<T> list = (items == null) ? Collections.emptyList() : items;
        return new ApiEnvelope<>("true", RESULT_OK, list, "null");
    }

    public static <T> ClinicApiEnvelope<List<T>> successClinicList(List<T> items) {
        List<T> list = (items == null) ? Collections.emptyList() : items;
        return new ClinicApiEnvelope<>(RESULT_OK, list);
    }

    public static <T> ApiEnvelope<T> successOne(String resultCode, T item) {
        List<T> list = (item == null) ? Collections.emptyList() : List.of(item);
        return new ApiEnvelope<>("true", resultCode, list, "null");
    }

    public static <T> ApiEnvelope<T> successList(String resultCode, List<T> items) {
        List<T> list = (items == null) ? Collections.emptyList() : items;
        return new ApiEnvelope<>("true", resultCode, list, "null");
    }

    public static ApiEnvelope<?> error(HttpStatus status, String message) {
        String code = (status == null) ? "9999" : String.valueOf(status.value());
        String msg = (message == null || message.isBlank()) ? "unknown_error" : message;
        return new ApiEnvelope<>("false", code, Collections.emptyList(), msg);
    }

    public static ApiEnvelope<?> error(String resultCode, String message) {
        String code = (resultCode == null || resultCode.isBlank()) ? "9999" : resultCode;
        String msg = (message == null || message.isBlank()) ? "unknown_error" : message;
        return new ApiEnvelope<>("false", code, Collections.emptyList(), msg);
    }

    @Data
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonPropertyOrder({"success", "result", "data", "error"})
    public static class ApiEnvelope<T> {
        private String success;
        private String result;
        private List<T> data;
        private String error;
    }

    @Getter
    @AllArgsConstructor
    public static class ClinicApiEnvelope<T> {
        private String result;
        private T data;

    }


}