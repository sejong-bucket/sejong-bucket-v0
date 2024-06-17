package com.ime.lockmanager.major.adapter.out.majordetail;

import com.ime.lockmanager.major.application.port.out.majordetail.MajorDetailQueryPort;
import com.ime.lockmanager.major.domain.MajorDetail;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class MajorDetailQueryRepository implements MajorDetailQueryPort {
    private final MajorDetailJpaRepository majorDetailJpaRepository;

    @Override
    public Optional<MajorDetail> findByNameWithMajor(String majorName) {
        return majorDetailJpaRepository.findByNameWithMajor(majorName);
    }

    @Override
    public List<MajorDetail> findAll() {
        return majorDetailJpaRepository.findAll();
    }

}
