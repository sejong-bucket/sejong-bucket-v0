package com.ime.lockmanager.user.application.service;

import com.ime.lockmanager.common.format.exception.user.NotFoundUserException;
import com.ime.lockmanager.locker.domain.lockerdetail.LockerDetail;
import com.ime.lockmanager.user.application.port.in.UserQueryUseCase;
import com.ime.lockmanager.user.application.port.in.req.UserInfoRequestDto;
import com.ime.lockmanager.user.application.port.out.UserQueryPort;
import com.ime.lockmanager.user.application.port.out.res.UserInfoQueryResponseDto;
import com.ime.lockmanager.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserQueryService implements UserQueryUseCase {
    private final UserQueryPort userQueryPort;

    @Override
    public UserInfoQueryResponseDto findUserInfoByStudentNum(UserInfoRequestDto userRequestDto) {
        User user = userQueryPort.findByIdWithMajorDetailAndMajor(userRequestDto.getUserId())
                .orElseThrow(NotFoundUserException::new);
        UserInfoQueryResponseDto extracted = buildUserInfoResponse(user);
        return extracted;
    }

    private UserInfoQueryResponseDto buildUserInfoResponse(User user) {
        UserInfoQueryResponseDto.UserInfoQueryResponseDtoBuilder userInfoBuilder = UserInfoQueryResponseDto.builder()
                .name(user.getName())
                .studentNum(user.getStudentNum())
                .userState(user.getUserState())
                .majorDetail(user.getMajorDetail().getName());

        setReservation(user,userInfoBuilder);

        return userInfoBuilder.build();
    }
    private void setReservation(User user,UserInfoQueryResponseDto.UserInfoQueryResponseDtoBuilder userInfoBuilder) {
        LockerDetail lockerDetail = user.getLockerDetail();
        if(lockerDetail==null){
            return;
        }
        userInfoBuilder
                .lockerName(lockerDetail.getLocker().getName())
                .lockerDetailNum(lockerDetail.getLockerNum())
                .lockerDetailId(lockerDetail.getId());
    }
}
