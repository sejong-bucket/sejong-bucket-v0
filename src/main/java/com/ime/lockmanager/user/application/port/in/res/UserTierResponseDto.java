package com.ime.lockmanager.user.application.port.in.res;

import com.ime.lockmanager.user.adapter.in.res.UserTierResponse;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class UserTierResponseDto {
    private Boolean isApprove;

    public UserTierResponse toResponse() {
        return UserTierResponse.builder()
                .isApprove(isApprove)
                .build();
    }

}
