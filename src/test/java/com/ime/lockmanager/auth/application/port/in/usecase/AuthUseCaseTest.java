package com.ime.lockmanager.auth.application.port.in.usecase;

import com.ime.lockmanager.auth.application.port.in.req.LoginRequestDto;
import com.ime.lockmanager.major.application.port.out.major.MajorCommandPort;
import com.ime.lockmanager.major.application.port.out.majordetail.MajorDetailCommandPort;
import com.ime.lockmanager.major.domain.Major;
import com.ime.lockmanager.major.domain.MajorDetail;
import com.ime.lockmanager.user.application.port.out.UserQueryPort;
import com.ime.lockmanager.user.domain.User;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static com.ime.lockmanager.user.domain.UserTier.MEMBER;
import static java.time.LocalDateTime.now;

@Slf4j
@ActiveProfiles("test")
@SpringBootTest
class AuthUseCaseTest {
    @Autowired
    private MajorCommandPort majorCommandPort;
    @Autowired
    private MajorDetailCommandPort majorDetailCommandPort;
    @Autowired
    private AuthUseCase authUseCase;
    @Autowired
    private UserQueryPort userQueryPort;

    @DisplayName("새로운 사용자가 여러 컴퓨터로 로그인할때 데이터 정합성테스트")
    @Test
    void userLoginConcurrencyTest() throws InterruptedException, IOException {
        //given
        log.info("동시성 테스트 준비");
        Major major = majorCommandPort.save(Major.builder().name("AI로봇학과").build());
        MajorDetail majorDetail = majorDetailCommandPort.save(
                MajorDetail.builder().major(major).name("무인이동체공학전공").build());

        LoginRequestDto requestDto = LoginRequestDto.builder().id("19011721").pw("dlwovy123!@#").build();

        int numberOfThread = 100;
        ExecutorService service = Executors.newFixedThreadPool(numberOfThread);
        CountDownLatch countDownLatch = new CountDownLatch(numberOfThread);

        //when
        log.info("lockerReserve 동시성 테스트 진행");
        for (int i=0;i<100;i++) {

            service.execute(
                    () -> {
                        try {
                            authUseCase.login(requestDto);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        } finally {
                            countDownLatch.countDown();
                        }
                    }
            );
        }

        countDownLatch.await();

        //then
        log.info("lockerReserve 동시성 테스트 검증");
        int count = 0;

        List<User> all = userQueryPort.findAll();
        List<User> collect = all.stream().filter(user -> user.getStudentNum().equals(requestDto.getId())).collect(Collectors.toList());


        log.info("끝났다~~");
        Assertions.assertThat(collect.size()).isEqualTo(1);
        log.info("검증됬다~~~~");
    }
}