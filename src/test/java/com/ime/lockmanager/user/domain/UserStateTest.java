package com.ime.lockmanager.user.domain;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UserStateTest {
    String ATTEND = "재학";
    String REST = "휴학";
    String GRADUATE = "졸업";
    @DisplayName("로그인시 외부api에서 받아오는 학생의 재학여부가 재학일때 enum타입으로 변환해주는 메서드 테스트")
    @Test
    void matchAttendTest(){
        //given
        //when
        UserState match = UserState.match(ATTEND);
        //then
        Assertions.assertThat(match).isEqualTo(UserState.ATTEND);
    }

    @DisplayName("로그인시 외부api에서 받아오는 학생의 재학여부가 휴학일때 enum타입으로 변환해주는 메서드 테스트")
    @Test
    void matchRestTest(){
        //given
        //when
        UserState match = UserState.match(REST);
        //then
        Assertions.assertThat(match).isEqualTo(UserState.REST);
    }
    @DisplayName("로그인시 외부api에서 받아오는 학생의 재학여부가 졸업일때 enum타입으로 변환해주는 메서드 테스트")
    @Test
    void matchGraduateTest(){
        //given
        //when
        UserState match = UserState.match(GRADUATE);
        //then
        Assertions.assertThat(match).isEqualTo(UserState.GRADUATE);
    }
}