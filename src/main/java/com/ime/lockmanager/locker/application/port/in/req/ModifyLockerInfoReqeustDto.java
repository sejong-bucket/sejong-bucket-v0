package com.ime.lockmanager.locker.application.port.in.req;

import com.ime.lockmanager.user.domain.UserState;
import lombok.Builder;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Getter
public class ModifyLockerInfoReqeustDto {
    private String lockerName;
    private Long lockerId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private List<UserState> userStates;
    private MultipartFile image;
}
