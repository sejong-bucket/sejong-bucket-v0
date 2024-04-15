package com.ime.lockmanager.user.application.port.in.res;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ApplyingStudentsDto {
    @Schema(defaultValue = "학생회비 납부 신청한 학생의 학번")
    private String studentNum;
    @Schema(defaultValue = "학생회비 납부 신청한 학생의 이름")
    private String studentName;
}
