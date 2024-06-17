package com.ime.lockmanager.major.adapter.out.major;

import com.ime.lockmanager.major.application.port.out.major.MajorCommandPort;
import com.ime.lockmanager.major.domain.Major;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class MajorCommandRepository implements MajorCommandPort {
    private final MajorJpaRepository majorJpaRepository;
    @Override
    public void deleteAll() {
        majorJpaRepository.deleteAll();
    }
}
