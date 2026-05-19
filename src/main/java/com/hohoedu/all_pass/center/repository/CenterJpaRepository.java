package com.hohoedu.all_pass.center.repository;

import com.hohoedu.all_pass.center.Center;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CenterJpaRepository extends JpaRepository<Center, Integer> {

    Optional<Center> findByCenterCode(String centerCode);
}