package com.ime.lockmanager.user.application.port.in;

import com.ime.lockmanager.user.application.port.in.req.FindAllUserRequestDto;
import com.ime.lockmanager.user.application.port.in.req.UserInfoRequestDto;
import com.ime.lockmanager.user.application.port.in.res.PagingApplyStudentsResponseDto;
import com.ime.lockmanager.user.application.port.out.res.AllUserInfoForAdminResponseDto;
import com.ime.lockmanager.user.application.port.out.res.UserInfoQueryResponseDto;
import org.springframework.data.domain.Page;

public interface UserQueryUseCase {
    UserInfoQueryResponseDto findUserInfoByStudentNum(UserInfoRequestDto userRequestDto);

    Page<AllUserInfoForAdminResponseDto> findAllUserInfo(FindAllUserRequestDto requestDto);

}
