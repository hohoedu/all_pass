package com.hohoedu.all_pass.user.repository;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.hohoedu.all_pass.user.model.User;

@Mapper
public interface UserRepository {
    public List<User> findAll();

    public void insert(User user);
}
