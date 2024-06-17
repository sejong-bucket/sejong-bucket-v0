package com.ime.lockmanager.reservation.adapter.out;

import com.ime.lockmanager.locker.domain.lockerdetail.LockerDetail;
import com.ime.lockmanager.reservation.adapter.out.dto.DeleteReservationByStudentNumDto;
import com.ime.lockmanager.reservation.application.port.out.ReservationQueryPort;
import com.ime.lockmanager.reservation.domain.Reservation;
import com.ime.lockmanager.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ReservationQueryRepository implements ReservationQueryPort {
    private final ReservationJpaRepository reservationJpaRepository;

    @Override
    public Optional<Reservation> findByUserId(Long userId) {
        return reservationJpaRepository.findByUserId(userId);
    }

    @Override
    public Optional<Reservation> findAllByUserIdAndLockerDetailId(Long userId, Long lockerDetailId) {
        return reservationJpaRepository.findAllByUserIdAndLockerDetailId(userId, lockerDetailId);
    }

    @Override
    public List<Reservation> findAllByLockerDetails(List<LockerDetail> lockerDetailsByLocker) {
        return reservationJpaRepository.findAllByLockerDetails(lockerDetailsByLocker);
    }


    @Override
    public Optional<Reservation> findByLockerDetailId(Long lockerDetailId) {
        return reservationJpaRepository.findByLockerDetailId(lockerDetailId);
    }
}
