package com.ime.lockmanager.common.webclient.sejong.service.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class SejongMemberResponseDto {
    private String msg;
    private SejongMemberResponseResult result;
}
