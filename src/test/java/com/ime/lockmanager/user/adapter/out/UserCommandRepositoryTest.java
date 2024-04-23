package com.ime.lockmanager.user.adapter.out;

import com.ime.lockmanager.major.adapter.out.major.MajorJpaRepository;
import com.ime.lockmanager.major.adapter.out.majordetail.MajorDetailJpaRepository;
import com.ime.lockmanager.major.domain.Major;
import com.ime.lockmanager.major.domain.MajorDetail;
import com.ime.lockmanager.user.domain.Role;
import com.ime.lockmanager.user.domain.User;
import com.ime.lockmanager.user.domain.UserState;
import com.ime.lockmanager.user.domain.UserTier;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@Slf4j
@DataJpaTest
class UserCommandRepositoryTest {
    private UserCommandRepository userCommandRepository;
    @Autowired
    private UserJpaRepository userJpaRepository;
    @Autowired
    private MajorDetailJpaRepository majorDetailJpaRepository;
    @Autowired
    private MajorJpaRepository majorJpaRepository;
    private Major major;
    private MajorDetail majorDetail;

    @AfterEach
    void reset() {
        userJpaRepository.deleteAll();
    }

    @BeforeEach
    void init() {
        major = majorJpaRepository.save(createMajor());
        majorDetail = majorDetailJpaRepository.save(createMajorDetail(major));
        userCommandRepository = new UserCommandRepository(userJpaRepository);
    }


    @DisplayName("모든 사용자 저장")
    @Test
    void saveAllTest() {
        //given

        List<User> users = new ArrayList<>();
        User user1 = createUser(UserState.ATTEND, UserTier.MEMBER, Role.ROLE_USER, "3");
        User user2 = createUser(UserState.ATTEND, UserTier.MEMBER, Role.ROLE_USER, "4");
        User user3 = createUser(UserState.ATTEND, UserTier.MEMBER, Role.ROLE_USER, "1");
        users.add(user1);
        users.add(user2);
        users.add(user3);
        //when
        List<User> userList = userCommandRepository.saveAll(users);
        //then
        Assertions.assertThat(userList.size()).isEqualTo(3);
    }

    @DisplayName("사용자 1명 저장 테스트")
    @Test
    void saveTest() {
        //given
        User user = createUser(UserState.ATTEND, UserTier.MEMBER, Role.ROLE_USER, "3");
        //when
        User save = userCommandRepository.save(user);
        //then
        org.junit.jupiter.api.Assertions.assertAll(
                () -> Assertions.assertThat(save.getId()).isNotNull(),
                ()-> Assertions.assertThat(save.getStudentNum()).isEqualTo(user.getStudentNum()),
                ()-> Assertions.assertThat(save.getUserTier()).isEqualTo(user.getUserTier()),
                ()-> Assertions.assertThat(save.getGrade()).isEqualTo(user.getGrade()),
                ()-> Assertions.assertThat(save.getRole()).isEqualTo(user.getRole()),
                ()-> Assertions.assertThat(save.getName()).isEqualTo(user.getName()),
                ()-> Assertions.assertThat(save.getMajorDetail()).isEqualTo(user.getMajorDetail())
                );
    }

    User createUser(UserState userState, UserTier userTier, Role userRole, String grade) {
        return User.builder()
                .name("test")
                .userState(userState)
                .userTier(userTier)
                .auth(true)
                .majorDetail(majorDetail)
                .studentNum("19011721")
                .id(1l)
                .role(userRole)
                .grade(grade)
                .build();
    }

    @DisplayName("저장된 사용자 전체를 삭제하는 메서드 테스트")
    @Test
    void deleteUserTest(){
        //given
        List<User> users = new ArrayList<>();
        User user1 = createUser(UserState.ATTEND, UserTier.MEMBER, Role.ROLE_USER, "3");
        User user2 = createUser(UserState.ATTEND, UserTier.MEMBER, Role.ROLE_USER, "4");
        User user3 = createUser(UserState.ATTEND, UserTier.MEMBER, Role.ROLE_USER, "1");
        users.add(user1);
        users.add(user2);
        users.add(user3);
        userCommandRepository.saveAll(users);
        //when
        userCommandRepository.deleteAll();
        //then
        List<User> all = userJpaRepository.findAll();
        Assertions.assertThat(all.size()).isZero();
    }

    private MajorDetail createMajorDetail(Major major) {
        return MajorDetail.builder().name("testDetail").major(major).build();
    }

    private Major createMajor() {
        return Major.builder().id(1l).name("testMajor").build();
    }
}