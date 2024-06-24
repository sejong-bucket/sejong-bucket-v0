package com.ime.lockmanager.locker.adapter.in.res.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class LockersInfoInMajorDto {

    @Schema(description = "사물함Id")
    private Long id;
    @Schema(description = "사물함을 예약할수 있는 학생회비 납부조건")
    private List<String> permitTiers;
    @Schema(description = "사물함을 예약할수 있는 학생의 재학조건")
    private String permitStates;
    @Schema(description = "사물함 이름")
    private String name;
    @Schema(description = "예약 시작시간")
    @JsonFormat(shape = JsonFormat.Shape.STRING,
            pattern = "yyyy-MM-dd HH:mm:ss",
            timezone = "Asia/Seoul")
    private LocalDateTime startReservationTime;
    @Schema(description = "예약 마감시간")
    @JsonFormat(shape = JsonFormat.Shape.STRING,
            pattern = "yyyy-MM-dd HH:mm:ss",
            timezone = "Asia/Seoul")
    private LocalDateTime endReservationTime;
    @Schema(description = "사물함 전체 행")
    private String totalRow;
    @Schema(description = "사물함 전체 열")
    private String totalColumn;
    @Schema(description = "사물함 이미지")
    private String image;
}
