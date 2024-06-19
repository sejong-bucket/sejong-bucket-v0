package com.ime.lockmanager.user.adapter.out;

import com.ime.lockmanager.config.QuerydslConfig;
import com.ime.lockmanager.major.adapter.out.major.MajorJpaRepository;
import com.ime.lockmanager.major.adapter.out.majordetail.MajorDetailJpaRepository;
import com.ime.lockmanager.major.domain.Major;
import com.ime.lockmanager.major.domain.MajorDetail;
import com.ime.lockmanager.user.domain.Role;
import com.ime.lockmanager.user.domain.User;
import com.ime.lockmanager.user.domain.UserState;
import com.ime.lockmanager.util.CreateEntityUtil;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@ActiveProfiles("test")
@DataJpaTest
@Import(QuerydslConfig.class)
class UserQueryRepositoryTest {
    private UserQueryRepository userQueryRepository;
    private Major major;
    private MajorDetail majorDetail;
    @Autowired
    private UserJpaRepository userJpaRepository;
    private UserQuerydslRepository userQuerydslRepository;
    @Autowired
    private JPAQueryFactory jpaQueryFactory;

    @Autowired
    private MajorDetailJpaRepository majorDetailJpaRepository;
    @Autowired
    private MajorJpaRepository majorJpaRepository;
    @Autowired
    private EntityManagerFactory factory;

    @Autowired
    private EntityManager em;

    @BeforeEach
    void setup() {
        userQuerydslRepository = new UserQuerydslRepository(jpaQueryFactory);
        userQueryRepository = new UserQueryRepository(userJpaRepository, userQuerydslRepository);
        major = majorJpaRepository.save(CreateEntityUtil.createMajor("testMajor"));
        majorDetail = majorDetailJpaRepository.save(CreateEntityUtil.createMajorDetail("testMajorDetail", major));
    }

    @AfterEach
    void clear() {
        em.clear();
        userJpaRepository.deleteAll();
    }

    @DisplayName("")
    @Test
    void test(){
        //given

        //when

        //then
    }

    /**
     * Todo
     * 조회할때 저장 100개 했지만 조회가 안됌 쿼리문제?? 무슨문제지..?
     */
    @DisplayName("학과에 속한 학번으로 검색된 특정 학생 페이징하여 조회 테스트")
    @Test
    void findSearchInMajorPagingAscTest() {
        //given
        List<User> userList = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            int studentNum = 19011721 + i;
            User user = CreateEntityUtil.createUser(UserState.ATTEND,
                    UserTier.MEMBER,
                    Role.ROLE_USER,
                    "3",
                    Integer.toString(studentNum),
                    "test" + i,
                    majorDetail);
            userList.add(user);
        }
        List<User> list = userJpaRepository.saveAll(userList);
        System.out.println(list.size());
        em.flush();
        int PAGE_SIZE=30;
        int page = 3;
        PageRequest pageRequest = PageRequest.of(page, PAGE_SIZE);
        //when
        Page<User> users = userQueryRepository.pagingByMajorASC(majorDetail.getMajor(), "19011724", pageRequest);
        //then
        Assertions.assertThat(users.getSize()).isEqualTo(1);
    }


    @DisplayName("검색어가 없을때 학과의 모든 학생 페이징하여 조회 테스트")
    @Test
    void findAllInMajorPagingAscTest() {
        //given
        List<User> userList = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            int studentNum = 19011721 + i;
            User user = CreateEntityUtil.createUser(UserState.ATTEND,
                    UserTier.MEMBER,
                    Role.ROLE_USER,
                    "3",
                    Integer.toString(studentNum),
                    "test" + i,
                    majorDetail);
            userList.add(user);
        }
        userJpaRepository.saveAll(userList);
        int PAGE_SIZE=30;
        int page = 3;
        PageRequest pageRequest = PageRequest.of(page, PAGE_SIZE);
        //when
        Page<User> users = userQueryRepository.pagingByMajorASC(majorDetail.getMajor(), null, pageRequest);
        //then
        Assertions.assertThat(users.getTotalElements()).isEqualTo(100);
        Assertions.assertThat(users.getTotalPages()).isEqualTo(userList.size()/PAGE_SIZE+(userList.size()%PAGE_SIZE>0?1:0));
        Assertions.assertThat(users.getSize()).isEqualTo(PAGE_SIZE);
    }

    @DisplayName("사용자가 존재하지 않을때 pk를 통해 사용자 조회")
    @Test
    void findNotExistUserByIdTest() {
        //given
        //when
        //then
        Assertions.assertThatThrownBy(() -> userQueryRepository.findById(1l).get()).isInstanceOf(NoSuchElementException.class);
    }

    @DisplayName("사용자가 존재할때 pk를 통해 사용자 조회")
    @Test
    void findUserByIdTest() {
        //given
        User user = CreateEntityUtil.createTestUser(UserState.ATTEND,
                UserTier.MEMBER,
                Role.ROLE_USER,
                "19011721",
                majorDetail);
        User save = userJpaRepository.save(user);
        em.flush();
        em.clear();
        System.out.println("------");
        //when
        Long id = save.getId();
        System.out.println("------");

        User findUser = userQueryRepository.findById(id).get();
        System.out.println("------");

        //then
        Assertions.assertThat(save.getId()).isEqualTo(findUser.getId());
    }

    @DisplayName("사용자pk로 사용자를 조회할때 존재하지 않는 사용자일때")
    @Test
    void findByIdWithMajorDetailAndMajorNotExist() {
        //given
        //when
        //then
        Assertions.assertThatThrownBy(() -> userQueryRepository.findByIdWithMajorDetailAndMajor(1l).get())
                .isInstanceOf(NoSuchElementException.class);
    }

    @DisplayName("사용자pk로 사용자를 조회할때 학과와 세부학과를 즉시로딩하는지")
    @Test
    void findByIdWithMajorDetailAndMajorExistEagerLoadingTest() {
        //given
        PersistenceUnitUtil persistenceUnitUtil = factory.getPersistenceUnitUtil();
        User user = CreateEntityUtil.createTestUser(UserState.ATTEND,
                UserTier.MEMBER,
                Role.ROLE_USER,
                "19011721",
                majorDetail);
        User save = userJpaRepository.save(user);
        em.flush();
        em.clear();
        //when
        User findUser = userQueryRepository.findByIdWithMajorDetailAndMajor(save.getId()).get();
        //then
        org.junit.jupiter.api.Assertions.assertAll(() -> Assertions.assertThat(findUser.getId()).isNotNull(),
                () -> Assertions.assertThat(findUser.getStudentNum()).isEqualTo(user.getStudentNum()),
                () -> Assertions.assertThat(findUser.getName()).isEqualTo(user.getName()));
        Assertions.assertThat(persistenceUnitUtil.isLoaded(findUser, "majorDetail")).isTrue();
        ;
        Assertions.assertThat(persistenceUnitUtil.isLoaded(findUser.getMajorDetail(), "major")).isTrue();
        ;
    }

    @DisplayName("조회하는 학번의 사용자가 있을때 학번으로 사용자를 조회하는 테스트")
    @Test
    void findByStudentNumExsistTest() {
        //given
        User user = CreateEntityUtil.createTestUser(UserState.ATTEND,
                UserTier.MEMBER,
                Role.ROLE_USER,
                "19011721",
                majorDetail);
        userJpaRepository.save(user);
        //when
        User findStudent = userQueryRepository.findByStudentNum(user.getStudentNum()).get();
        //then
        Assertions.assertThat(findStudent.getStudentNum()).isEqualTo(user.getStudentNum());
        Assertions.assertThat(findStudent.getId()).isNotNull();
    }

    @DisplayName("조회하는 학번의 사용자가 없을 때 학번으로 사용자를 조회하는 예외 테스트")
    @Test
    void findByStudentNumNotExsistTest() {
        //given
        User user = CreateEntityUtil.createTestUser(UserState.ATTEND,
                UserTier.MEMBER,
                Role.ROLE_USER,
                "19011721",
                majorDetail);
//        userJpaRepository.save(user);
        //when
        //then
        Assertions.assertThatThrownBy(() -> userQueryRepository.findByStudentNum(user.getStudentNum()).get())
                .isInstanceOf(NoSuchElementException.class);
    }

    @DisplayName("사용자가 존재할때 사용자 전체 조회테스트")
    @Test
    void userfindAllTestNotZero() {
        //given
        User user1 = CreateEntityUtil.createTestUser(UserState.ATTEND,
                UserTier.MEMBER,
                Role.ROLE_USER,
                "19011721",
                majorDetail);
        User user2 = CreateEntityUtil.createTestUser(UserState.ATTEND,
                UserTier.MEMBER,
                Role.ROLE_USER,
                "19011722",
                majorDetail);
        User user3 = CreateEntityUtil.createTestUser(UserState.ATTEND,
                UserTier.MEMBER,
                Role.ROLE_USER,
                "19011723",
                majorDetail);
        //when
        List<User> list = List.of(user1, user2, user3);
        userJpaRepository.saveAll(list);
        //then
        List<User> userList = userQueryRepository.findAll();
        Assertions.assertThat(userList.size()).isEqualTo(list.size());
    }

    @DisplayName("사용자가 존재할때 사용자 전체 조회테스트")
    @Test
    void userfindAllTestZero() {
        //given
        //then
        List<User> userList = userQueryRepository.findAll();
        Assertions.assertThat(userList.size()).isZero();
    }
}