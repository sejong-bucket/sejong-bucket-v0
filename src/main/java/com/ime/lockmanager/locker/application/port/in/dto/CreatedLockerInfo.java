package com.ime.lockmanager.locker.application.port.in.dto;

import com.ime.lockmanager.user.domain.UserState;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Getter
@Schema(description = "생성된 사물함 정보 응답DTO")
public class CreatedLockerInfo {
    @Schema(description = "사물함Id")
    private Long id;
    @Schema(description = "사물함을 예약할수 있는 학생의 재학조건")
    private List<UserState> permitStates;
    @Schema(description = "사물함 이름")
    private String name;
    @Schema(description = "예약 시작시간")
    private LocalDateTime startReservationTime;
    @Schema(description = "예약 마감시간")
    private LocalDateTime endReservationTime;
    @Schema(description = "사물함 전체 행")
    private String totalRow;
    @Schema(description = "사물함 전체 열")
    private String totalColumn;
    @Schema(description = "사물함 이미지")
    private String image;
}
