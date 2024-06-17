package com.ime.lockmanager.major.application.port.out.major;

import com.ime.lockmanager.major.domain.Major;

import java.util.List;
import java.util.Optional;

public interface MajorQueryPort {
    Optional<Major> findById(Long majorId);
}
