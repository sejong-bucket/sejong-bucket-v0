package com.ime.lockmanager.major.adapter.out.majordetail;

import com.ime.lockmanager.major.application.port.out.majordetail.MajorDetailCommandPort;
import com.ime.lockmanager.major.domain.MajorDetail;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class MajorDetailCommandRepository implements MajorDetailCommandPort {
    private final MajorDetailJpaRepository majorDetailJpaRepository;
    @Override
    public void deleteAll() {
        majorDetailJpaRepository.deleteAll();
    }
}
