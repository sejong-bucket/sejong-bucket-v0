package com.ime.lockmanager.user.application.service;

import com.ime.lockmanager.common.format.exception.user.NotFoundUserException;
import com.ime.lockmanager.locker.application.port.in.req.LockerRegisterRequestDto;
import com.ime.lockmanager.reservation.application.port.in.ReservationCommandUseCase;
import com.ime.lockmanager.user.application.port.in.UserCommandUseCase;
import com.ime.lockmanager.user.application.port.in.dto.UpdateUserDueInfoDto;
import com.ime.lockmanager.user.application.port.in.req.*;
import com.ime.lockmanager.user.application.port.in.res.UserTierResponseDto;
import com.ime.lockmanager.user.application.port.out.UserCommandPort;
import com.ime.lockmanager.user.application.port.out.UserQueryPort;
import com.ime.lockmanager.user.domain.Role;
import com.ime.lockmanager.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
class UserCommandService implements UserCommandUseCase {
    private final UserQueryPort userQueryPort;
    private final ReservationCommandUseCase reservationCommandUseCase;
    private final int PAGE_SIZE = 30;

    /**
     * Todo
     * save, saveAll같이 update도 jpa의 트랜잭션을 묶는게 있는지 확인하고 성능 비교해보기
     * 벌크연산 적용하고, 성능비교하기
     * @param requestDto
     * @throws Exception
     */
    @Override
    public void modifiedUserInfo(ModifiedUserInfoRequestDto requestDto) throws Exception {
        for (ModifiedUserInfoDto modifiedUserInfo : requestDto.getModifiedUserInfoList()) {
            User user = getMaybeUserByStudentNum(modifiedUserInfo.getStudentNum())
                    .orElseThrow(NotFoundUserException::new);
            if (modifiedUserInfo.getLockerDetailId() != null) {
                reservationCommandUseCase.reserveForAdmin(
                        LockerRegisterRequestDto.builder() //일반예약은 lockerdetail의 PK값을 받아서 예약하는것이지만, 지금은 lockerdetail의 칸번호를 받고있으니 수정해야함
                                .userId(user.getId())
                                .lockerDetailId(modifiedUserInfo.getLockerDetailId())
                                .build()
                );
            }
            if (modifiedUserInfo.getAdmin() != null) {
                user.changeAdmin(modifiedUserInfo.getAdmin().booleanValue());
            }
        }
    }

    private Optional<User> getMaybeUserByStudentNum(String studentNum) {
        return userQueryPort.findByStudentNum(studentNum);
    }
}
