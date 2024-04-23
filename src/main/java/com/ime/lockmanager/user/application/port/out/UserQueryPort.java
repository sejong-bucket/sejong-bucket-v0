package com.ime.lockmanager.user.application.port.out;

import com.ime.lockmanager.major.domain.Major;
import com.ime.lockmanager.user.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public interface UserQueryPort {
    Page<User> findApplicantsByMajorOrderByStudentNumAsc(Major major, Pageable pageable);

    Page<User> pagingByMajorASC(Major major, String search, Pageable pageable);
    Optional<User> findByStudentNum(String studentNum);

    List<User> findAll();

    Optional<User> findById(Long userId);
    Optional<User> findByIdWithMajorDetailAndMajor(Long userId);
}
