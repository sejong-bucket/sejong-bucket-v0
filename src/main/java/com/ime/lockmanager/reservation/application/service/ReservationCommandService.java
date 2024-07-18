package com.ime.lockmanager.reservation.application.service;

import com.ime.lockmanager.common.aop.meta.DistributeLock;
import com.ime.lockmanager.common.aop.meta.ReserveLock;
import com.ime.lockmanager.common.format.exception.locker.*;
import com.ime.lockmanager.common.format.exception.reservation.NotFoundReservationException;
import com.ime.lockmanager.common.format.exception.user.AlreadyReservedUserException;
import com.ime.lockmanager.common.format.exception.user.InvalidReservedStatusException;
import com.ime.lockmanager.common.format.exception.user.NotFoundUserException;
import com.ime.lockmanager.locker.application.port.in.req.LockerRegisterRequestDto;
import com.ime.lockmanager.locker.application.port.in.res.LockerRegisterResponseDto;
import com.ime.lockmanager.locker.application.port.out.LockerDetailQueryPort;
import com.ime.lockmanager.locker.domain.locker.Locker;
import com.ime.lockmanager.locker.domain.lockerdetail.LockerDetail;
import com.ime.lockmanager.reservation.application.port.in.ReservationCommandUseCase;
import com.ime.lockmanager.reservation.application.port.in.req.ChangeReservationRequestDto;
import com.ime.lockmanager.user.application.port.in.req.UserCancelLockerRequestDto;
import com.ime.lockmanager.user.application.port.out.UserQueryPort;
import com.ime.lockmanager.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;


@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class ReservationCommandService implements ReservationCommandUseCase {
    private final UserQueryPort userQueryPort;
    private final LockerDetailQueryPort lockerDetailQueryPort;
    private final RedisTemplate<String, String> redisTemplate;

    private static final String LOCKER_KEY = "LOCKER_";

    @Override
    public void resetReservation(Long lockerId) {
        log.info("---------사물함 초기화----------");
        List<LockerDetail> lockerDetails = lockerDetailQueryPort.findByLockerId(lockerId);

        runReset(lockerDetails);
        log.info("---------사물함 초기화----------");
    }

    private void runReset(List<LockerDetail> lockerDetails) {
        lockerDetails.forEach(LockerDetail::cancel);
    }

    @Override
    @ReserveLock(identifier = LOCKER_KEY, key = "#dto.lockerDetailId")
    public LockerRegisterResponseDto reserveForUser(LockerRegisterRequestDto dto) throws Exception {
        User user = getUserById(dto.getUserId());
        LockerDetail lockerDetail = getLockerDetailById(dto.getLockerDetailId());
        log.info("(사용자)예약 시작 : [학번 {}, 사물함 번호 {}]", user.getStudentNum(), lockerDetail.getLockerNum());
        Locker locker = getLockerFromLockerDetail(lockerDetail);
        verifyDistinctConditionForReserve(locker);
        runReserve(user, lockerDetail, locker);
        log.info("예약 완료 : [학번 {}, 사물함 번호 {}]", user.getStudentNum(), lockerDetail.getLockerNum());
        return LockerRegisterResponseDto
                .of(lockerDetail.getLockerNum(), user.getStudentNum(), locker.getName());
    }

    private User getUserById(Long userId) {
        return userQueryPort.findById(userId).orElseThrow(NotFoundUserException::new);
    }

    private LockerDetail getLockerDetailById(Long lockerDetailId) {
        return lockerDetailQueryPort.findByIdWithLocker(lockerDetailId)
                .orElseThrow(InvalidLockerDetailException::new);
    }

    private Locker getLockerFromLockerDetail(LockerDetail lockerDetail) {
        return lockerDetail.getLocker();
    }

    private Long runReserve(User user, LockerDetail lockerDetail, Locker locker) {
        verifyCommonConditionForReserve(user, lockerDetail, locker);
        lockerDetail.register(user);
        return lockerDetail.getId();
    }

    private void verifyDistinctConditionForReserve(Locker locker) {
        if (!locker.isDeadlineValid()) {
            throw new IsNotReserveTimeException();
        }
    }

    private void verifyCommonConditionForReserve(User user, LockerDetail lockerDetail, Locker locker) {
        verifyLockerConditions(user, lockerDetail, locker);
        verifyUserConditions(user);
    }

    private void verifyUserConditions(User user) {
        if (isReservationNegativeByUser(user)) {
            throw new AlreadyReservedUserException();
        }
    }

    private void verifyLockerConditions(User user, LockerDetail lockerDetail, Locker locker) {
        if (!locker.getPermitUserState().equals(user.getUserState())) {
            throw new InvalidReservedStatusException();
        }
        if (isReservationNegativeByLockerDetailId(lockerDetail)) {
            throw new AlreadyReservedLockerException();
        }
    }

    private boolean isReservationNegativeByLockerDetailId(LockerDetail lockerDetail) {
        if (lockerDetail.isReserve()) {
            return true;
        }
        return false;
    }

    private boolean isReservationNegativeByUser(User user) {
        if (user.isReserve()) {
            return true;
        }
        return false;
    }


    public Long cancelLockerByStudentNum(UserCancelLockerRequestDto cancelLockerDto) {
        log.info("{} : 사물함 취소시작", cancelLockerDto.getUserId());
        LockerDetail lockerDetail = lockerDetailQueryPort.findById(cancelLockerDto.getLockerDetailId())
                .orElseThrow(() -> new RuntimeException("예약 안된 사물함입니다."));
        Long cancelLockerDetailId = lockerDetail.cancel();
        redisTemplate.delete(LOCKER_KEY + cancelLockerDetailId);
        log.info("{} : 사물함 취소끝", cancelLockerDto.getUserId());
        return cancelLockerDetailId;
    }
}
