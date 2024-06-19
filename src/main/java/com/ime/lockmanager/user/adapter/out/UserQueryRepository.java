package com.ime.lockmanager.user.adapter.out;

import com.ime.lockmanager.auth.application.port.out.AuthToUserQueryPort;
import com.ime.lockmanager.major.domain.Major;
import com.ime.lockmanager.user.application.port.out.UserQueryPort;
import com.ime.lockmanager.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class UserQueryRepository implements UserQueryPort, AuthToUserQueryPort {
    private final UserJpaRepository userJpaRepository;
    private final UserQuerydslRepository userQuerydslRepository;



    /**
     * Todo
     * 데이터 조회에 에러가 나서 조치예정
     */
    @Override
    public Page<User> pagingByMajorASC(Major major, String search, Pageable pageable) {
        return userQuerydslRepository.pagingAndSearchUserInMajorASC(major, search, pageable);
    }
    @Override
    public Optional<User> findByIdWithMajorDetailAndMajor(Long userId) {
        return userJpaRepository.findByIdWithMajorDetailAndMajor(userId);
    }

    @Override
    public Optional<User> findById(Long userId) {
        return userJpaRepository.findById(userId);
    }

    @Override
    public List<User> findAll() {
        return userJpaRepository.findAll();
    }

    @Override
    public Optional<User> findByStudentNum(String studentNum) {
        return userJpaRepository.findByStudentNum(studentNum);
    }
}
