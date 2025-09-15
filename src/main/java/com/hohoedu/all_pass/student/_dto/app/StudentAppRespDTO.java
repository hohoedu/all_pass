package com.hohoedu.all_pass.student._dto.app;

import lombok.Builder;
import lombok.Data;

@Data
public class StudentAppRespDTO {

    @Data
    public static class AppLoginRespDTO {
        private String stuid;       // 학생 아이디
        private String name;        // 학생 이름
        private String cid;         // 센터 코드
        private String cname;       // 센터 이름
        private String brotherGb;   // 형제 구분
        private String sibling;     // 형제
        private String firstlogin;  // 첫 로그인
        private String profileimg;  // 프로필 사진
        private String hak;         // 학년
        private String ihak;        // 북 코드
        private String appid;       // 앱 아이디

        @Builder
        public AppLoginRespDTO(String stuid, String name, String cid, String cname, String brotherGb, String sibling, String firstlogin, String profileimg, String hak, String ihak, String appid) {
            this.stuid = stuid;
            this.name = name;
            this.cid = cid;
            this.cname = cname;
            this.brotherGb = brotherGb;
            this.sibling = sibling;
            this.firstlogin = firstlogin;
            this.profileimg = profileimg;
            this.hak = hak;
            this.ihak = ihak;
            this.appid = appid;
        }
    }

    @Data
    public static class AppLoginViewDTO {
        private String studentId;   // 학생 아이디
        private String studentName; // 학생 이름
        private String centerCode;  // 센터 코드
        private String centerName;  // 센터 이름
        private String brotherGb;   // 형제 구분
        private String sibling;     // 형제
        private String firstlogin;  // 첫 로그인
        private String profileimg;  // 프로필 사진
        private String gradeKey;    // 학년
        private String ihak;        // 북 코드
        private String appId;       // 앱 아이디
        private String appPassword; // 앱 패스워드
    }
}
