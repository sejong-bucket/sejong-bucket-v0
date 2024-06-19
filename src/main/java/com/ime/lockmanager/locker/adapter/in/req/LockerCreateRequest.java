package com.ime.lockmanager.locker.adapter.in.req;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "사물함 생성시 요청 DTO")
@Getter
public class LockerCreateRequest {
    @Schema(description = "생성할 사물함 이름")
    @NotBlank(message = "사물함 이름을 설정해주세요.")
    private String lockerName;
    @Schema(description = "생성할 사물함의 전체 행 개수")
    @NotBlank(message = "사물함의 전체 행의 개수를 설정해주세요.")
    private String totalRow;
    @Schema(description = "생성할 사물함의 전체 열 개수")
    @NotBlank(message = "사물함의 전체 열의 개수를 설정해주세요.")
    private String totalColumn;
    @Schema(description = "생성할 사물함의 예약 시작 시간")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @NotNull(message = "사물함 예약 시작 시간을 설정해주세요.")
    private LocalDateTime startReservationTime;
    @Schema(description = "생성할 사물함의 예약 마감 시간")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @NotNull(message = "사물함 예약 마감 시간을 설정해주세요.")
    private LocalDateTime endReservationTime;
    @Schema(description = "사물함 번호 증가방향")
    @NotNull(message = "사물함 번호의 증가방향을 설정해주세요")
    private NumberIncreaseDirection numberIncreaseDirection;
}
