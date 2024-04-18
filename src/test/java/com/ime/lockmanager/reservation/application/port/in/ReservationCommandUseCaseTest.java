package com.ime.lockmanager.reservation.application.port.in;

import com.ime.lockmanager.locker.adapter.in.res.LockersInfoInMajorResponse;
import com.ime.lockmanager.locker.adapter.in.res.dto.LockersInfoDto;
import com.ime.lockmanager.locker.application.port.in.LockerUseCase;
import com.ime.lockmanager.locker.application.port.in.req.FindAllLockerInMajorRequestDto;
import com.ime.lockmanager.locker.application.port.in.req.LockerCreateRequestDto;
import com.ime.lockmanager.locker.application.port.in.req.LockerRegisterRequestDto;
import com.ime.lockmanager.locker.application.port.in.res.LockerCreateResponseDto;
import com.ime.lockmanager.locker.application.port.out.LockerCommandPort;
import com.ime.lockmanager.locker.application.port.out.LockerDetailCommandPort;
import com.ime.lockmanager.locker.application.port.out.LockerDetailQueryPort;
import com.ime.lockmanager.locker.application.port.out.LockerQueryPort;
import com.ime.lockmanager.locker.domain.lockerdetail.LockerDetail;
import com.ime.lockmanager.locker.domain.lockerdetail.dto.LockerDetailInfo;
import com.ime.lockmanager.major.application.port.out.major.MajorCommandPort;
import com.ime.lockmanager.major.application.port.out.majordetail.MajorDetailCommandPort;
import com.ime.lockmanager.major.application.port.out.majordetail.MajorDetailQueryPort;
import com.ime.lockmanager.major.application.port.out.major.MajorQueryPort;
import com.ime.lockmanager.major.domain.Major;
import com.ime.lockmanager.major.domain.MajorDetail;
import com.ime.lockmanager.reservation.application.port.in.req.ChangeReservationRequestDto;
import com.ime.lockmanager.reservation.application.port.out.ReservationCommandPort;
import com.ime.lockmanager.reservation.application.port.out.ReservationQueryPort;
import com.ime.lockmanager.reservation.domain.Reservation;
import com.ime.lockmanager.user.application.port.out.UserCommandPort;
import com.ime.lockmanager.user.application.port.out.UserQueryPort;
import com.ime.lockmanager.user.domain.Role;
import com.ime.lockmanager.user.domain.User;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
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
import static com.ime.lockmanager.user.domain.UserTier.MEMBER;
import static java.time.LocalDateTime.now;

@Slf4j
@ActiveProfiles("test")
@SpringBootTest
class ReservationCommandUseCaseTest {
    @Autowired
    private LockerDetailQueryPort lockerDetailQueryPort;
    @Autowired
    private ReservationQueryPort reservationQueryPort;
    @Autowired
    private UserQueryPort userQueryPort;
    @Autowired
    private LockerUseCase lockerUseCase;
    @Autowired
    private MajorQueryPort majorQueryPort;
    @Autowired
    private MajorDetailQueryPort majorDetailQueryPort;
    @Autowired
    private ReservationCommandUseCase reservationCommandUseCase;
    @Autowired
    private LockerQueryPort lockerQueryPort;
    @Autowired
    ReservationCommandPort reservationCommandPort;
    @Autowired
    LockerDetailCommandPort lockerDetailCommandPort;
    @Autowired
    LockerCommandPort lockerCommandPort;
    @Autowired
    UserCommandPort userCommandPort;
    @Autowired
    MajorCommandPort majorCommandPort;

    @Autowired
    MajorDetailCommandPort majorDetailCommandPort;
    @AfterEach
    void init(){
        log.info("------------------1");
        reservationCommandPort.deleteAll();
        lockerDetailCommandPort.deleteAll();
        lockerCommandPort.deleteAll();
        userCommandPort.deleteAll();
        log.info("------------------1");
    }

    @DisplayName("수정시 동시성 테스트")
    @Test
    void changeLockerConcurrencyTest() throws Exception {
        //given
        log.info("동시성 테스트 준비");
        Major major = majorCommandPort.save(Major.builder().name("AI로봇학과").build());
        MajorDetail majorDetail = majorDetailCommandPort.save(
                MajorDetail.builder().major(major).name("무인이동체공학전공").build());
        List<Long> userIds = new ArrayList<>();

        log.info("사용자 생성");
        for (int i = 0; i < 100; i++) {
            String name = Integer.toString(i);
            Role role = Role.ROLE_USER;
            String status = "재학";
            String studentNum = Integer.toString(19011721 + i);
            boolean membership = true;
            User save = userCommandPort.save(
                    User.builder()
                            .majorDetail(majorDetail)
                            .userTier(MEMBER)
                            .userState(ATTEND)
                            .studentNum(studentNum)
                            .role(role)
                            .name(name)
                            .build()
            );
            userIds.add(save.getId());
            log.info(studentNum);
        }

        log.info("사물함 생성");
        LockerCreateResponseDto savedLocker = lockerUseCase.createLocker(LockerCreateRequestDto.builder()
                .endReservationTime(now().plusDays(1))
                .startReservationTime(now().minusDays(1))
                .lockerName("test")
                .totalColumn("15")
                .totalRow("10")
                .numberIncreaseDirection(DOWN)
                .userStates(List.of(ATTEND))
                .userTiers(List.of(MEMBER))
                .build(), major.getId());

        List<LockerDetail> lockerDetails = lockerDetailQueryPort
                .findByLockerId(savedLocker.getCreatedLockerId());

        for (int i = 0; i < userIds.size(); i++) {
            reservationCommandUseCase.reserveForUser(LockerRegisterRequestDto.of(major.getId(), userIds.get(i), lockerDetails.get(i).getId()));
        }


        //when

        log.info("lockerReserve 동시성 테스트 진행");

        int numberOfThread = 100;
        ExecutorService service = Executors.newFixedThreadPool(numberOfThread);
        CountDownLatch countDownLatch = new CountDownLatch(numberOfThread);

        for (Long userId : userIds) {

            service.execute(
                    () -> {
                        try {
                            Reservation reservation = reservationQueryPort.findByUserId(userId).orElseThrow(NullPointerException::new);

                            reservationCommandUseCase.changeReservation(ChangeReservationRequestDto.of(lockerDetails.get(userIds.size()).getId(),
                                    reservation.getLockerDetail().getId(), userId, major.getId()));

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

        for (Long userId : userIds) {
            Optional<Reservation> reservation = reservationQueryPort.findByUserId(userId);
            if(reservation.get().getLockerDetail().getId().equals(lockerDetails.get(userIds.size()).getId())){
                log.info("예약번호 : " + reservation.get().getId());
                count++;
            }
        }

        log.info("끝났다~~");
        Assertions.assertThat(count).isEqualTo(1);
        log.info("검증됬다~~~~");
    }

    @DisplayName("여러명이 동시에 같은 사물함을 예약할때 데이터 정합성테스트")
    @Test
    void reserveConcurrencyTest() throws InterruptedException, IOException {
        //given
        log.info("동시성 테스트 준비");
        Major major = majorCommandPort.save(Major.builder().name("AI로봇학과").build());
        MajorDetail majorDetail = majorDetailCommandPort.save(
                MajorDetail.builder().major(major).name("무인이동체공학전공").build());
        List<Long> userIds = new ArrayList<>();

        log.info("사용자 생성");
        for (int i = 0; i < 100; i++) {
            String name = Integer.toString(i);
            Role role = Role.ROLE_USER;
            String status = "재학";
            String studentNum = Integer.toString(19011721 + i);
            boolean membership = true;
            User save = userCommandPort.save(
                    User.builder()
                            .majorDetail(majorDetail)
                            .userTier(MEMBER)
                            .userState(ATTEND)
                            .studentNum(studentNum)
                            .role(role)
                            .name(name)
                            .build()
            );
            userIds.add(save.getId());
            log.info(studentNum);
        }

        log.info("사물함 생성");
        LockerCreateResponseDto savedLocker = lockerUseCase.createLocker(LockerCreateRequestDto.builder()
                .endReservationTime(now().plusDays(1))
                .startReservationTime(now().minusDays(1))
                .lockerName("test")
                .totalColumn("15")
                .totalRow("10")
                .numberIncreaseDirection(DOWN)
                .userStates(List.of(ATTEND))
                .userTiers(List.of(MEMBER))
                .build(), major.getId());

        List<LockerDetail> lockerDetails = lockerDetailQueryPort
                .findByLockerId(savedLocker.getCreatedLockerId());

        int numberOfThread = 100;
        ExecutorService service = Executors.newFixedThreadPool(numberOfThread);
        CountDownLatch countDownLatch = new CountDownLatch(numberOfThread);

        //when
        log.info("lockerReserve 동시성 테스트 진행");
        for (Long userId : userIds) {

            service.execute(
                    () -> {
                        try {
                            reservationCommandUseCase.reserveForUser(LockerRegisterRequestDto.builder()
                                    .userId(userId)
                                    .lockerDetailId(lockerDetails.get(0).getId())
                                    .majorId(major.getId())
                                    .build());

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

        for (Long userId : userIds) {
            Optional<Reservation> reservation = reservationQueryPort.findByUserId(userId);
            if (reservation.isPresent()) {
                log.info("예약번호 : " + reservation.get().getId());
                count++;
            }
        }

        log.info("끝났다~~");
        Assertions.assertThat(count).isEqualTo(1);
        log.info("검증됬다~~~~");
    }




    @DisplayName("사물함 예약시 쿼리 확인")
    @Test
    void verifyQueryReservation() throws IOException {
        //given
        log.info("동시성 테스트 준비");
        Major major = majorCommandPort.save(Major.builder().name("AI로봇학과").build());
        MajorDetail majorDetail = majorDetailCommandPort.save(
                MajorDetail.builder().major(major).name("무인이동체공학전공").build());

        log.info("사물함 생성");
        LockerCreateResponseDto savedLocker = lockerUseCase.createLocker(LockerCreateRequestDto.builder()
                .endReservationTime(now().plusDays(1))
                .startReservationTime(now().minusDays(1))
                .lockerName("test")
                .totalColumn("15")
                .totalRow("10")
                .numberIncreaseDirection(DOWN)
                .userStates(List.of(ATTEND))
                .userTiers(List.of(MEMBER))
                .build(), major.getId());

        LockerCreateResponseDto savedLocker2 = lockerUseCase.createLocker(LockerCreateRequestDto.builder()
                .endReservationTime(now().plusDays(1))
                .startReservationTime(now().minusDays(1))
                .lockerName("test2")
                .totalColumn("15")
                .totalRow("10")
                .numberIncreaseDirection(DOWN)
                .userStates(List.of(ATTEND))
                .userTiers(List.of(MEMBER))
                .build(), major.getId());

        log.info("사용자 생성");
        User save = userCommandPort.save(
                User.builder()
                        .majorDetail(majorDetail)
                        .userTier(MEMBER)
                        .userState(ATTEND)
                        .studentNum("19011721")
                        .role(Role.ROLE_ADMIN)
                        .name("이재표")
                        .build()
        );

        //when
        log.info("쿼리나감");

        LockersInfoInMajorResponse allLockerInMajor = lockerUseCase.findAllLockerInMajor(FindAllLockerInMajorRequestDto.builder()
                .userId(save.getId())
                .build());

        for (LockersInfoDto lockersInfoDto : allLockerInMajor.getLockersInfo()) {
            System.out.println(lockersInfoDto.getLocker().getName());
            for (LockerDetailInfo lockerDetailInfo : lockersInfoDto.getLockerDetail()) {
                System.out.print(lockerDetailInfo.getLockerNum());
                System.out.println(", ");
            }
            System.out.println("-----------------");
        }
        log.info("쿼리끝남");
        //then
    }


}
