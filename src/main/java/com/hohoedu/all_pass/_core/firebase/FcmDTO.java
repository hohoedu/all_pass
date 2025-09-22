package com.hohoedu.all_pass._core.firebase;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class FcmDTO {
    private List<String> tokens;
    private String title;
    private String body;

}
