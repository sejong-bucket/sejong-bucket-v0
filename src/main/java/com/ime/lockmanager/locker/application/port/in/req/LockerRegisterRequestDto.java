package com.ime.lockmanager.locker.application.port.in.req;

import com.ime.lockmanager.locker.application.port.in.res.LockerRegisterResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class LockerRegisterRequestDto {

        private Long userId;
        private Long majorId;
        private Long lockerDetailId;

    public static LockerRegisterRequestDto of(Long majorId,Long userId,Long lockerDetailId){
        return LockerRegisterRequestDto.builder()
                .majorId(majorId)
                .userId(userId)
                .lockerDetailId(lockerDetailId)
                .build();
    }
}
