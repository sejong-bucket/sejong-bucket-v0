package com.ime.lockmanager.locker.application.port.in;

import com.ime.lockmanager.locker.adapter.in.res.LockersInfoInMajorResponse;
import com.ime.lockmanager.locker.application.port.in.req.*;
import com.ime.lockmanager.locker.application.port.in.res.LeftLockerResponseDto;
import com.ime.lockmanager.locker.application.port.in.res.LockerCreateResponseDto;

import java.io.IOException;

public interface LockerUseCase {

    LeftLockerResponseDto getCreatedLockers(Long majorId);

    LockerCreateResponseDto createLocker(LockerCreateRequestDto lockerCreateRequestDto, Long majorId) throws IOException;

    LockersInfoInMajorResponse findAllLockerInMajor(FindAllLockerInMajorRequestDto build);

}
