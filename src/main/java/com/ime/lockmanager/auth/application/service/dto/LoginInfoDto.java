package com.ime.lockmanager.auth.application.service.dto;

import com.ime.lockmanager.major.domain.MajorDetail;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class LoginInfoDto {
    private String name;
    private String status;
    private String studentNum;
    private String grade;
    private MajorDetail majorDetail;
}
