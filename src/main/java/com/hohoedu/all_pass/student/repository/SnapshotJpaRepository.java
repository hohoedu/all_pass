package com.hohoedu.all_pass.student.repository;

import com.hohoedu.all_pass.student.model.StudentSnapshot;
import com.hohoedu.all_pass.student.model.StudentSnapshotId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SnapshotJpaRepository extends JpaRepository<StudentSnapshot, StudentSnapshotId> {

    Optional<StudentSnapshot> findByIdSnapshotYmAndIdCenterCode(String snapshotYm, String centerCode);

    List<StudentSnapshot> findByIdCenterCodeAndIdSnapshotYmBetweenOrderByIdSnapshotYm(String centerCode, String fromYm, String toYm);
}