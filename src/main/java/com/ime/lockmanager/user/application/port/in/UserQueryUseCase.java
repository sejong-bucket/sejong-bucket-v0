package com.ime.lockmanager.user.application.port.in;

import com.ime.lockmanager.user.application.port.in.req.UserInfoRequestDto;
import com.ime.lockmanager.user.application.port.out.res.UserInfoQueryResponseDto;

public interface UserQueryUseCase {
    UserInfoQueryResponseDto findUserInfoByStudentNum(UserInfoRequestDto userRequestDto);
}
