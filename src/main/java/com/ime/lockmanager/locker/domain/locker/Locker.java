package com.ime.lockmanager.locker.domain.locker;

import com.ime.lockmanager.common.domain.BaseTimeEntity;
import com.ime.lockmanager.locker.domain.Period;
import com.ime.lockmanager.locker.domain.locker.dto.LockerCreateDto;
import com.ime.lockmanager.major.domain.Major;
import com.ime.lockmanager.user.domain.UserState;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import javax.persistence.*;

import java.util.ArrayList;
import java.util.List;

import static java.time.LocalDateTime.now;
import static javax.persistence.FetchType.LAZY;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Entity(name = "LOCKER_TABLE")
public class Locker extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Schema(name = "사물함 예약 기간")
    @Embedded
    private Period period;

    @Schema(name = "사물함명")
    private String name;

    @Schema(name = "사물함 보유 학과")
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "major_id")
    private Major major;

    private String imageUrl;
    private String totalRow;
    private String totalColumn;

    @ElementCollection(targetClass = UserState.class)
    @JoinTable(name = "PERMIT_USER_STATE_TABLE", joinColumns = @JoinColumn(name = "locker_id"))
    @Column(name = "permitUserState")
    @Enumerated(EnumType.STRING)
    private List<UserState> permitUserState = new ArrayList<>();

    public void modifiedDateTime(Period period) {
        this.period = period;
    }


    public static Locker createLocker(LockerCreateDto lockercreateDto) {
        return Locker.builder()
                .period(new Period(lockercreateDto.getStartReservationTime(), lockercreateDto.getEndReservationTime()))
                .name(lockercreateDto.getLockerName())
                .major(lockercreateDto.getMajor())
                .totalColumn(lockercreateDto.getTotalColumn())
                .totalRow(lockercreateDto.getTotalRow())
                .imageUrl(lockercreateDto.getImageUrl())
                .permitUserState(lockercreateDto.getUserStates())
                .build();
    }

    public boolean isDeadlineValid() {
        return this.period.getEndDateTime().isAfter(now()) &&
                this.period.getStartDateTime().isBefore(now());
    }
    public void rename(String rename){
        this.name = rename;
    }
}
