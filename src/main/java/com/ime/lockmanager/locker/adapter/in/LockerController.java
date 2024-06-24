package com.ime.lockmanager.locker.adapter.in;

import com.ime.lockmanager.common.format.success.SuccessResponse;
import com.ime.lockmanager.locker.adapter.in.res.LockersInfoInMajorResponse;
import com.ime.lockmanager.locker.application.port.in.LockerUseCase;
import com.ime.lockmanager.locker.application.port.in.req.FindAllLockerInMajorRequestDto;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import springfox.documentation.annotations.ApiIgnore;

import static org.springframework.http.MediaType.TEXT_EVENT_STREAM_VALUE;


@RequiredArgsConstructor
@RestController
@RequestMapping("${api.user.prefix}")
class LockerController {

    private final LockerUseCase lockerUseCase;

    @ApiOperation(
            value = "사물함 정보조회",
            notes = "사물함 이름, 기간, 각 사물함 칸의 예약여부정보"
    )
    @GetMapping("/majors/{majorId}/lockers")
    public SuccessResponse<LockersInfoInMajorResponse> findAllLockerInMajor(@PathVariable Long majorId) {
        long startTime = System.currentTimeMillis();
        SuccessResponse successResponse = new SuccessResponse(lockerUseCase.findAllLockerInMajor(FindAllLockerInMajorRequestDto.builder()
                .majorId(majorId).build()));
        long stopTime = System.currentTimeMillis();
        System.out.println("코드 실행 시간: " + (stopTime - startTime));
        return successResponse;

    }
}
