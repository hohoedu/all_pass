package com.hohoedu.all_pass.class_instance.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hohoedu.all_pass.class_instance.model.UnitCode;

import java.util.Optional;

public interface UnitCodeJpaRepository extends JpaRepository<UnitCode, Integer> {
    Optional<UnitCode> findByUnitKey(String unitKey);
}
