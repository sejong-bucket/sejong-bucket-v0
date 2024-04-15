package com.ime.lockmanager.user.adapter.in;

import com.ime.lockmanager.common.format.success.SuccessResponse;
import com.ime.lockmanager.user.adapter.in.res.UserInfoResponse;
import com.ime.lockmanager.user.application.port.in.UserQueryUseCase;
import com.ime.lockmanager.user.application.port.in.UserCommandUseCase;
import com.ime.lockmanager.user.application.port.in.req.UserInfoRequestDto;
import com.ime.lockmanager.user.application.port.out.res.UserInfoQueryResponseDto;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.security.Principal;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("${api.user.prefix}/users")
class UserController {

    private final UserCommandUseCase userCommandUseCase;
    private final UserQueryUseCase userQueryUseCase;

    @ApiOperation(
            value = "사용자 정보 조회",
            notes = "마이페이지접속시 사용자 정보조회 API"
    )
    @GetMapping("/{userId}")
    public SuccessResponse<UserInfoResponse> findUserInfo(@PathVariable Long userId) throws Exception {
        UserInfoQueryResponseDto userInfo = userQueryUseCase.findUserInfoByStudentNum(
                UserInfoRequestDto.builder()
                        .userId(userId)
                        .build());
        return new SuccessResponse(UserInfoResponse.fromResponseDto(
                userInfo
        ));
    }


    @ApiOperation(value = "학생회비 납부 신청")
    @PostMapping("/{userId}/membership")
    public SuccessResponse applyMembership(@PathVariable Long userId) {
        userCommandUseCase.applyMembership(userId);
        return SuccessResponse.ok();
    }
}
