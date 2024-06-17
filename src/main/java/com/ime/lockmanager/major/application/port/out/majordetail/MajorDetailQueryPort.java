package com.ime.lockmanager.major.application.port.out.majordetail;

import com.ime.lockmanager.major.domain.MajorDetail;

import java.util.List;
import java.util.Optional;

public interface MajorDetailQueryPort {
    Optional<MajorDetail> findByNameWithMajor(String majorName);

    List<MajorDetail> findAll();
}
