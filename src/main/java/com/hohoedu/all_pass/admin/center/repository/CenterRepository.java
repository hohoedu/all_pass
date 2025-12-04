package com.hohoedu.all_pass.admin.center.repository;

import com.hohoedu.all_pass.admin.center.Center;

import org.springframework.data.jpa.repository.JpaRepository;


import java.util.Optional;

public interface CenterRepository extends JpaRepository<Center, Integer> {
    Optional<Center> findByCenterCode(String centerCode);
}