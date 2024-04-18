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
import com.ime.lockmanager.user.domain.UserTier;
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
    private final UserCommandPort userCommandPort;
    private final ReservationCommandUseCase reservationCommandUseCase;
    private final int PAGE_SIZE = 30;

    @Override
    public UserTierResponseDto determineApplying(DetermineApplyingRequestDto requestDto, boolean isApprove) {
        User student = getMaybeUserByStudentNum(requestDto.getStudentNum())
                .orElseThrow(NotFoundUserException::new);
        if (isApprove) {
            student.approve();
        } else {
            student.deny();
        }
        return UserTierResponseDto.builder()
                .isApprove(isApprove)
                .build();
    }

    @Override
    public void applyMembership(Long userId) {
        User student = userQueryPort.findById(userId)
                .orElseThrow(NotFoundUserException::new);
        verifyAlreadyMember(student);
        student.applyMembership();
    }

    private void verifyAlreadyMember(User student) {
        if (!student.getUserTier().equals(UserTier.NON_MEMBER)) {
            throw new IllegalStateException("이미 신청 또느 승인된 학우입니다.");
        }
    }

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
            if (modifiedUserInfo.getMembership() != null) {
                if (modifiedUserInfo.getMembership().booleanValue() == Boolean.TRUE) {//납부자로 변경하고싶을때
                    user.approve();
                } else {
                    user.deny();
                }
            }
        }
    }

    @Override
    public void updateUserDueInfoOrSave(List<UpdateUserDueInfoDto> updateUserDueInfoDto) throws Exception {
        List<User> newUsers = updateUserDueInfoDto.parallelStream()
                .filter(dto -> getMaybeUserByStudentNum(dto.getStudentNum()).isEmpty())
                .map(dto -> User.builder()
                        .name(dto.getName())
                        .studentNum(dto.getStudentNum())
                        .userTier(UserTier.judge(dto.isDue()))
                        .role(Role.ROLE_USER)
                        .majorDetail(dto.getMajorDetail())
                        .auth(false)
                        .build()).collect(Collectors.toList());
        userCommandPort.saveAll(newUsers);
    }

    private Optional<User> getMaybeUserByStudentNum(String studentNum) {
        return userQueryPort.findByStudentNum(studentNum);
    }
}
