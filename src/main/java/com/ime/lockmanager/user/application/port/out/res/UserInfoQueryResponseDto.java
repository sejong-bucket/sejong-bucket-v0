package com.ime.lockmanager.user.application.port.out.res;

import com.ime.lockmanager.user.domain.UserState;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class UserInfoQueryResponseDto {
    private String name;
    private UserState userState;
    private String status;
    private String studentNum;

    private String lockerDetailNum;
    private String lockerName;
    private Long lockerDetailId;
    private String majorDetail;
    @Builder
    public UserInfoQueryResponseDto(Long lockerDetailId, String name,  String status, String studentNum, String lockerDetailNum, String lockerName, String majorDetail, UserState userState) {
        this.userState = userState;
        this.name = name;
        this.status = status;
        this.studentNum = studentNum;
        this.majorDetail = majorDetail;
        if(lockerDetailNum !=null){
            this.lockerDetailId = lockerDetailId;
            this.lockerDetailNum = lockerDetailNum;
            this.lockerName = lockerName;
        }else {
            this.lockerDetailId = null;
            this.lockerDetailNum = null;
            this.lockerName = null;
        }
    }
}
