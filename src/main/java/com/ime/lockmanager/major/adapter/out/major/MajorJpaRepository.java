package com.ime.lockmanager.major.adapter.out.major;

import com.ime.lockmanager.major.domain.Major;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MajorJpaRepository extends JpaRepository<Major,Long> {
    Optional<Major> findById(Long aLong);
}
