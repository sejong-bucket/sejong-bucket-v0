package com.ime.lockmanager.user.adapter.in.res;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserInfoAdminResponse {
    private UserInfo userInfo;
    private ReservationInfo reservationInfo;

}
