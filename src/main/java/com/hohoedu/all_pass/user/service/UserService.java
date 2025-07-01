package com.hohoedu.all_pass.user.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hohoedu.all_pass.user.model.User;
import com.hohoedu.all_pass.user.repository.UserJpaRepository;
import com.hohoedu.all_pass.user.repository.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserJpaRepository userJpaRepository; 
    @Autowired
    private UserRepository userRepository;

    public List<User> findAllByJpa() {
        List<User> user = userJpaRepository.findAll();
        return user;
    }

    public List<User> findAllByMyBatis() {
        List<User> user = userRepository.findAll();
        return user;
    }

    public void insert(User user) {
        System.out.println(user.getUserRole());
        userRepository.insert(user);
    }

}
