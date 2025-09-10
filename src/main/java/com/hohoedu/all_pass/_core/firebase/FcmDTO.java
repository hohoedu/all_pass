package com.hohoedu.all_pass._core.firebase;

import lombok.Data;

import java.util.Map;

@Data
public class FcmDTO {
    private String token;
    private String title;
    private String body;

}
