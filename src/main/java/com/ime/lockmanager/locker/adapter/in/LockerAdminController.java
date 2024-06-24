package com.ime.lockmanager.locker.adapter.in;

import com.ime.lockmanager.common.format.success.SuccessResponse;
import com.ime.lockmanager.locker.adapter.in.req.LockerCreateRequest;
import com.ime.lockmanager.locker.adapter.in.req.ModifyLockerInfoReqeust;
import com.ime.lockmanager.locker.adapter.in.res.LockerCreateResponse;
import com.ime.lockmanager.locker.application.port.in.LockerUseCase;
import com.ime.lockmanager.locker.application.port.in.req.LockerCreateRequestDto;
import com.ime.lockmanager.locker.application.port.in.res.CreatedLockersInfoResponse;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("${api.admin.prefix}")
class LockerAdminController {
    private final LockerUseCase lockerUseCase;

    @ApiOperation(
            value = "[관리자페이지] 생성된 사물함 정보조회 api"
    )
    @GetMapping("/majors/{majorId}/lockers")
    public SuccessResponse<CreatedLockersInfoResponse> getCreatedLockers(@ApiIgnore Authentication authentication,
                                                                         @PathVariable Long majorId) {
        return new SuccessResponse(lockerUseCase.getCreatedLockers(majorId).toResponse());
    }

    @ApiOperation(
            value = "새로운 사물함 생성",
            notes = "새로운 사물함을 생성하는 API"
    )
    @PostMapping("/majors/{majorId}/lockers")
    public SuccessResponse<LockerCreateResponse> createLocker(/*@ApiIgnore Authentication authentication,*/
                                                              @PathVariable Long majorId,
                                                              @Valid @RequestBody LockerCreateRequest lockerCreateRequest) throws IOException {
//        log.info("{} : 새로운 사물함 생성", authentication.getName());
        String createdLockerName = lockerUseCase.createLocker(
                        LockerCreateRequestDto
                                .fromRequest(lockerCreateRequest), majorId
                )
                .getCreatedLockerName();
        return new SuccessResponse(
                LockerCreateResponse.builder()
                        .createdLockerName(createdLockerName)
                        .build()
        );
    }
}
