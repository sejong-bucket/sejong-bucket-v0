package com.ime.lockmanager.reservation.application.port.in;

import com.ime.lockmanager.locker.adapter.in.res.LockersInfoInMajorResponse;
import com.ime.lockmanager.locker.adapter.in.res.dto.LockersInfoDto;
import com.ime.lockmanager.locker.adapter.out.LockerDetailJpaRepository;
import com.ime.lockmanager.locker.adapter.out.LockerJpaRepository;
import com.ime.lockmanager.locker.application.port.in.LockerUseCase;
import com.ime.lockmanager.locker.application.port.in.req.FindAllLockerInMajorRequestDto;
import com.ime.lockmanager.locker.application.port.in.req.LockerCreateRequestDto;
import com.ime.lockmanager.locker.application.port.in.req.LockerRegisterRequestDto;
import com.ime.lockmanager.locker.application.port.in.res.LockerCreateResponseDto;
import com.ime.lockmanager.locker.application.port.out.LockerCommandPort;
import com.ime.lockmanager.locker.application.port.out.LockerDetailCommandPort;
import com.ime.lockmanager.locker.application.port.out.LockerDetailQueryPort;
import com.ime.lockmanager.locker.domain.lockerdetail.LockerDetail;
import com.ime.lockmanager.locker.domain.lockerdetail.dto.LockerDetailInfo;
import com.ime.lockmanager.major.adapter.out.major.MajorJpaRepository;
import com.ime.lockmanager.major.adapter.out.majordetail.MajorDetailJpaRepository;
import com.ime.lockmanager.major.application.port.out.major.MajorCommandPort;
import com.ime.lockmanager.major.application.port.out.majordetail.MajorDetailCommandPort;
import com.ime.lockmanager.major.domain.Major;
import com.ime.lockmanager.major.domain.MajorDetail;
import com.ime.lockmanager.reservation.application.port.in.req.ChangeReservationRequestDto;
import com.ime.lockmanager.user.adapter.out.UserJpaRepository;
import com.ime.lockmanager.user.application.port.out.UserCommandPort;
import com.ime.lockmanager.user.domain.Role;
import com.ime.lockmanager.user.domain.User;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.ime.lockmanager.locker.adapter.in.req.NumberIncreaseDirection.DOWN;
import static com.ime.lockmanager.user.domain.UserState.ATTEND;
import static java.time.LocalDateTime.now;

@Slf4j
@ActiveProfiles("test")
@SpringBootTest
class ReservationCommandUseCaseTest {
    @Autowired
    private LockerDetailQueryPort lockerDetailQueryPort;
    @Autowired
    private LockerUseCase lockerUseCase;
    @Autowired
    private ReservationCommandUseCase reservationCommandUseCase;
    @Autowired
    UserCommandPort userCommandPort;

    @Autowired
    LockerDetailJpaRepository lockerDetailJpaRepository;
    @Autowired
    LockerJpaRepository lockerJpaRepository;
    @Autowired
    UserJpaRepository userJpaRepository;
    @Autowired
    MajorJpaRepository majorJpaRepository;
    @Autowired
    MajorDetailJpaRepository majorDetailJpaRepository;

    @BeforeEach
    void init() {
        log.info("------------------1");
        lockerDetailJpaRepository.deleteAll();
        lockerJpaRepository.deleteAll();
        userJpaRepository.deleteAll();
        majorDetailJpaRepository.deleteAll();
        majorJpaRepository.deleteAll();
        log.info("------------------1");
    }

    @DisplayName("여러 명이 동시에 같은 사물함을 예약할 때 데이터 정합성 테스트")
    @Test
    void reserveConcurrencyTest() throws InterruptedException, IOException {
        // given
        log.info("동시성 테스트 준비");
        Major major = majorJpaRepository.save(Major.builder().name("AI로봇학과").build());
        MajorDetail majorDetail = majorDetailJpaRepository.save(
                MajorDetail.builder().major(major).name("무인이동체공학전공").build());
        List<Long> userIds = new ArrayList<>();

        log.info("사용자 생성");
        for (int i = 0; i < 100; i++) {
            String name = "testname " + i;
            Role role = Role.ROLE_USER;
            String studentNum = Integer.toString(19011721 + i);
            User save = userCommandPort.save(
                    User.builder()
                            .majorDetail(majorDetail)
                            .userState(ATTEND)
                            .studentNum(studentNum)
                            .role(role)
                            .grade("4")
                            .auth(true)
                            .name(name)
                            .build()
            );
            userIds.add(save.getId());
            log.info(studentNum);
        }

        log.info("사물함 생성");
        LockerCreateResponseDto savedLocker = lockerUseCase.createLocker(LockerCreateRequestDto.builder()
                .lockerName("test")
                .totalRow("10")
                .totalColumn("15")
                .startReservationTime(now().minusDays(1))
                .endReservationTime(now().plusDays(1))
                .numberIncreaseDirection(DOWN)
                .build(), major.getId());

        List<LockerDetail> lockerDetails = lockerDetailQueryPort.findByLockerId(savedLocker.getCreatedLockerId());

        int numberOfThread = 1000;
        ExecutorService service = Executors.newFixedThreadPool(numberOfThread);
        CountDownLatch countDownLatch = new CountDownLatch(numberOfThread);

        // when
        log.info("lockerReserve 동시성 테스트 진행");
        long startTime = System.currentTimeMillis();

        for (Long userId : userIds) {
            service.execute(() -> {
                try {
                    reservationCommandUseCase.reserveForUser(LockerRegisterRequestDto.builder()
                            .userId(userId)
                            .lockerDetailId(lockerDetails.get(0).getId())
                            .majorId(major.getId())
                            .build());
                } catch (Exception e) {
                    log.error("예외 발생", e); // 예외 로그 기록
                    throw new RuntimeException(e);
                } finally {
                    countDownLatch.countDown();
                }
            });
        }

        countDownLatch.await();
        service.shutdown(); // 스레드 풀 종료

        long stopTime = System.currentTimeMillis();
        System.out.println("코드 실행 시간: " + (stopTime - startTime));

        // then
        log.info("lockerReserve 동시성 테스트 검증");
        int count = 0;

        List<LockerDetail> lockerDetailsInLocker = lockerDetailQueryPort.findByLockerId(savedLocker.getCreatedLockerId());
        for (LockerDetail lockerDetail : lockerDetailsInLocker) {
            User user = lockerDetail.getUser();
            if (user == null) {
                continue;
            }
            log.info(user.getName());
            count++;
        }

        log.info("끝났다~~");
        Assertions.assertThat(count).isEqualTo(1);
        log.info("검증됐다~~~~");
    }


}
