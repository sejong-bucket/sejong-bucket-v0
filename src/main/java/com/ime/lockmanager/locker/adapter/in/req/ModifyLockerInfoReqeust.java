package com.ime.lockmanager.locker.adapter.in.req;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ime.lockmanager.locker.application.port.in.req.ModifyLockerInfoReqeustDto;
import com.ime.lockmanager.user.domain.UserState;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class ModifyLockerInfoReqeust {
    private String lockerName;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
    private List<UserState> userStates;

    public ModifyLockerInfoReqeustDto toReqeustDto(Long lockerId, MultipartFile image) {
        return ModifyLockerInfoReqeustDto.builder()
                .lockerName(lockerName)
                .lockerId(lockerId)
                .startTime(startTime)
                .endTime(endTime)
                .image(image)
                .userStates(userStates)
                .build();
    }

}
