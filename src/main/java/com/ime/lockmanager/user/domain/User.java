package com.ime.lockmanager.user.domain;

import com.ime.lockmanager.common.domain.BaseTimeEntity;
import com.ime.lockmanager.major.domain.MajorDetail;
import com.ime.lockmanager.reservation.domain.Reservation;
import com.ime.lockmanager.user.domain.dto.UpdateUserInfoDto;
import lombok.*;

import javax.persistence.*;

import static com.ime.lockmanager.user.domain.Role.ROLE_ADMIN;
import static com.ime.lockmanager.user.domain.Role.ROLE_USER;
import static javax.persistence.EnumType.STRING;
import static javax.persistence.FetchType.LAZY;

@Getter
@Builder
@Entity(name = "USER_TABLE")
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "USER_ID")
    private Long id;

    @Column(name = "NAME")
    private String name;
    @Column(name = "STUDENT_NUM")
    private String studentNum;

    @Enumerated(STRING)
    private UserState userState;

    @Enumerated(STRING)
    @Column(nullable = false)
    private Role role;
    private String grade;
    @OneToOne(fetch = LAZY)
    @JoinColumn(name = "major_detail_id")
    private MajorDetail majorDetail;
    private boolean auth;

    public void changeAdmin(boolean isAdmin) {
        if (isAdmin) {
            this.role = ROLE_ADMIN;
        } else {
            this.role = ROLE_USER;
        }
    }

    public void updateUserInfo(UpdateUserInfoDto updateUserInfoDto) {
        this.auth = updateUserInfoDto.isAuth();
        this.userState = updateUserInfoDto.getStatus();
        this.grade = updateUserInfoDto.getGrade();
        this.majorDetail = updateUserInfoDto.getMajorDetail();
    }
}
