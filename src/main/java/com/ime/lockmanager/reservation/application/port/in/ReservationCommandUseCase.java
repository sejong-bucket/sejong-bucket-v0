package com.ime.lockmanager.reservation.application.port.in;

import com.ime.lockmanager.locker.application.port.in.req.LockerRegisterRequestDto;
import com.ime.lockmanager.locker.application.port.in.res.LockerRegisterResponseDto;
import com.ime.lockmanager.reservation.application.port.in.req.ChangeReservationRequestDto;
import com.ime.lockmanager.user.application.port.in.req.UserCancelLockerRequestDto;

public interface ReservationCommandUseCase {
    LockerRegisterResponseDto reserveForUser(LockerRegisterRequestDto lockerRegisterRequestDto) throws Exception;

    void resetReservation(Long lockerId);

    Long cancelLockerByStudentNum(UserCancelLockerRequestDto build);

}
