package com.ime.lockmanager.user.adapter.in.res;

import com.ime.lockmanager.user.application.port.out.res.UserInfoQueryResponseDto;
import com.ime.lockmanager.user.domain.UserState;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserInfoResponse {

    private String userName;
    private String studentNum;
    private UserState userState;
    private String reservedLockerDetailNum;
    private Long reservedLockerDetailId;
    private String reservedLockerName;
    private String majorDetail;
    public static UserInfoResponse fromResponseDto(UserInfoQueryResponseDto userInfoQueryResponseDto) {
        return UserInfoResponse.builder()
                .reservedLockerDetailNum(userInfoQueryResponseDto.getLockerDetailNum())
                .studentNum(userInfoQueryResponseDto.getStudentNum())
                .userName(userInfoQueryResponseDto.getName())
                .majorDetail(userInfoQueryResponseDto.getMajorDetail())
                .reservedLockerName(userInfoQueryResponseDto.getLockerName())
                .reservedLockerDetailId(userInfoQueryResponseDto.getLockerDetailId())
                .userState(userInfoQueryResponseDto.getUserState())
                .build();
    }
}
