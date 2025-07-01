package com.hohoedu.all_pass.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hohoedu.all_pass.user.model.User;

public interface UserJpaRepository extends JpaRepository<User, Integer>{

}
