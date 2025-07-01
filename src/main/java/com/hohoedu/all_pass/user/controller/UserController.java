package com.hohoedu.all_pass.user.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.hohoedu.all_pass.center.model.Center;
import com.hohoedu.all_pass.user.code.UserRoleCode;
import com.hohoedu.all_pass.user.model.User;
import com.hohoedu.all_pass.user.service.UserService;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @ResponseBody
    @GetMapping("/jpa")
    public List<User> allUsersByJpa() {
        return userService.findAllByJpa();
    }

    @ResponseBody
    @GetMapping("/mybatis")
    public List<User> allUsersByMyBatis() {

        return userService.findAllByMyBatis();
    }

    @ResponseBody
    @PostMapping("/join")
    public void joinUser() {
        User user = User.builder()
                .userNo(null)
                .userId("love")
                .password("4321")
                .username("러브")
                .userRole(UserRoleCode.builder()
                        .userRoleNo(3)
                        .build())
                .center(Center.builder()
                        .centerNo("ULS001")
                        .build())
                .createdAt(null)
                .build();

        userService.insert(user);

    }

}
