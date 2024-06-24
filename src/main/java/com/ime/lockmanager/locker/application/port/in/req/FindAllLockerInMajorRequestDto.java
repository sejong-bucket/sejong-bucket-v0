package com.ime.lockmanager.locker.application.port.in.req;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FindAllLockerInMajorRequestDto {
    private Long majorId;
}
