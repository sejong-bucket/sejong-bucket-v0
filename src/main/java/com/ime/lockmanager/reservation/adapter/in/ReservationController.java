package com.ime.lockmanager.reservation.adapter.in;

import com.ime.lockmanager.common.format.success.SuccessResponse;
import com.ime.lockmanager.locker.application.port.in.req.LockerRegisterRequestDto;
import com.ime.lockmanager.locker.adapter.in.res.LockerRegisterResponse;
import com.ime.lockmanager.reservation.adapter.in.req.LockerDetailCancelRequest;
import com.ime.lockmanager.reservation.adapter.in.res.CancelLockerDetailResponse;
import com.ime.lockmanager.reservation.adapter.in.res.ChangeReservationResponse;
import com.ime.lockmanager.reservation.application.port.in.ReservationCommandUseCase;
import com.ime.lockmanager.reservation.application.port.in.req.ChangeReservationRequestDto;
import com.ime.lockmanager.user.application.port.in.req.UserCancelLockerRequestDto;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.security.Principal;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("${api.user.prefix}")
public class ReservationController {
    private final ReservationCommandUseCase reservationCommandUseCase;

    @ApiOperation(
            value = "사물함 취소",
            notes = "현재 사용자의 예약된 사물함을 취소하는 API"
    )
    @PatchMapping("/lockerDetail/{lockerDetailId}/reservations")
    public SuccessResponse<CancelLockerDetailResponse> cancelLocker(@ApiIgnore Principal principal,
                                                                    @PathVariable Long lockerDetailId,
                                                                    @Valid @RequestBody LockerDetailCancelRequest request) {
        log.info("{} : 사물함 취소", principal.getName());
        Long cancelLockerByStudentId = reservationCommandUseCase.cancelLockerByStudentNum(
                UserCancelLockerRequestDto.of(request.getUserId(), lockerDetailId)
        );
        return new SuccessResponse(CancelLockerDetailResponse.builder()
                .canceledLockerDetailNum(cancelLockerByStudentId)
                .studentNum(principal.getName()).build());
    }

    //사물함 예약하는 api
    @ApiOperation(
            value = "사물함 예약",
            notes = "사용자가 사물함을 선택할시 해당 사물함을 예약하는 API"
    )
    @PostMapping("/users/{userId}/majors/{majorId}/lockerDetail/{lockerDetailId}/reservations")
    public SuccessResponse<LockerRegisterResponse> registerLocker(
            @PathVariable Long userId,
            @PathVariable Long lockerDetailId,
            @PathVariable Long majorId) throws Exception {
//        log.info("{} : 시믈함 예약진행", principal.getName());
        LockerRegisterResponse lockerRegisterResponse = LockerRegisterResponse.fromResponse(
                reservationCommandUseCase.reserveForUser(LockerRegisterRequestDto.of(majorId, userId, lockerDetailId))
        );
        log.info("{} : {}의 {}번 예약완료",
                lockerRegisterResponse.getStudentNum(),
                lockerRegisterResponse.getLockerName(),
                lockerRegisterResponse.getLockerDetailNum());
        return new SuccessResponse(lockerRegisterResponse);
    }

    @ApiOperation(
            value = "사물함 예약변경",
            notes = "사용자가 다른 사물함으로 변경을 희망할때 사용하는 API"
    )
    @PatchMapping("/users/{userId}/majors/{majorId}/lockerDetail/{originLockerDtailId}/reservations/change")
    public SuccessResponse<ChangeReservationResponse> changeReservation(@ApiIgnore Principal principal,
                                                                        @PathVariable Long userId,
                                                                        @PathVariable Long originLockerDtailId,
                                                                        @PathVariable Long majorId,
                                                                        @RequestParam Long newLockerDetailId) {
        log.info("{} : 사물함 취소", principal.getName());
        Long changedLockerDetailId = reservationCommandUseCase.changeReservation(
                ChangeReservationRequestDto.of(newLockerDetailId, originLockerDtailId, userId, majorId)
        );
        return new SuccessResponse(ChangeReservationResponse.builder()
                .changeLockerId(changedLockerDetailId)
                .build());
    }
}
