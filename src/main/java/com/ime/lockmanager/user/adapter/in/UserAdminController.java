package com.ime.lockmanager.user.adapter.in;

import com.ime.lockmanager.common.format.success.SuccessResponse;
import com.ime.lockmanager.user.adapter.in.req.DetermineApplyingRequest;
import com.ime.lockmanager.user.adapter.in.req.ModifiedUserInfoRequest;
import com.ime.lockmanager.user.adapter.in.res.AllApplyingStudentPageResponse;
import com.ime.lockmanager.user.adapter.in.res.UserInfoAdminPageResponse;
import com.ime.lockmanager.user.adapter.in.res.UserTierResponse;
import com.ime.lockmanager.user.application.port.in.UserQueryUseCase;
import com.ime.lockmanager.user.application.port.in.UserCommandUseCase;
import com.ime.lockmanager.user.application.port.in.req.FindAllUserRequestDto;
import com.ime.lockmanager.user.application.port.out.res.AllUserInfoForAdminResponseDto;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("${api.admin.prefix}")
class UserAdminController {

    private final UserCommandUseCase userCommandUseCase;
    private final UserQueryUseCase userQueryUseCase;

    @ApiOperation(
            value = "모든 사용자의 정보를 조회",
            notes = "모든 사용자의 정보를 조회하여 반환해주는 API(관리자용),검색기능 추가예정,uri 수정될수 있음"
    )
    @ApiImplicitParam(
            name = "page"
            , value = "원하는 페이지번호"
            , required = true
            , dataType = "int"
            , defaultValue = "0")
    @GetMapping("/majors/{majorId}/users")
    public SuccessResponse<UserInfoAdminPageResponse> adminInfo(@PathVariable Long majorId,
                                                                @RequestParam(name = "page", defaultValue = "0") int page,
                                                                @RequestParam(name = "search", required = false) String search) {
        Page<AllUserInfoForAdminResponseDto> allUserInfo = userQueryUseCase.findAllUserInfo(FindAllUserRequestDto
                .of(majorId, search, page));
        return new SuccessResponse(
                UserInfoAdminPageResponse.builder()
                        .adminResponse(allUserInfo.stream()
                                .map(allUserInfoForAdminResponseDto -> allUserInfoForAdminResponseDto.toResponse())
                                .collect(Collectors.toList()))
                        .currentPage(allUserInfo.getNumber())
                        .totalPage(allUserInfo.getTotalPages())
                        .currentElementSize(allUserInfo.getNumberOfElements())
                        .build()
        );

    }

    @ApiOperation(
            value = "수정된 사용자의 정보를 받아 실제 dB에 업데이트해주는 API(관리자용)"
    )
    @PatchMapping("/users")
    public SuccessResponse modifiedUserInfo(@Valid @RequestBody ModifiedUserInfoRequest modifiedUserInfoRequest)
            throws Exception {
        userCommandUseCase.modifiedUserInfo(modifiedUserInfoRequest.toRequestDto());
        return SuccessResponse.ok();
    }

    @ApiImplicitParam(
            name = "page"
            , value = "원하는 페이지번호"
            , required = true
            , dataType = "int"
            , defaultValue = "0")
    @ApiOperation(value = "학생회비 납부 신청자 조회")
    @GetMapping("/users/{userId}tier/apply")
    public SuccessResponse<AllApplyingStudentPageResponse> findAllApplyingStudent(@PathVariable Long userId,@RequestParam(name = "page",
            defaultValue = "0") int page) {
        AllApplyingStudentPageResponse response = userQueryUseCase.findApplyStudentsInMajorByPage(userId, page)
                .toResponse();
        return new SuccessResponse(response);
    }

    @ApiOperation(value = "학생회비 납부 신청자 승인 및 거절 API")
    @ApiImplicitParam(
            name = "isApprove"
            , value = "학생회비 납부 승인 또는 거절하기 위한 boolean값"
            , required = true
            , dataType = "boolean")
    @PostMapping("/users/tier/apply")
    public SuccessResponse<UserTierResponse> determineApplying(@Valid @RequestBody DetermineApplyingRequest request,
                                                               @RequestParam(name = "isApprove") boolean isApprove) {
        UserTierResponse response = userCommandUseCase.determineApplying(request.toRequestDto(), isApprove).toResponse();
        return new SuccessResponse(response);
    }
}
