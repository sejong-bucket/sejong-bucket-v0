package com.ime.lockmanager.user.domain;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserTierTest {
    @Test
    void judgeMember(){
        //given
        //when
        UserTier judgeUserTier = UserTier.judge(true);
        //then
        Assertions.assertThat(judgeUserTier).isEqualTo(UserTier.MEMBER);
    }

    @Test
    void judgeNonMember(){
        //given
        //when
        UserTier judgeUserTier = UserTier.judge(false);
        //then
        Assertions.assertThat(judgeUserTier).isEqualTo(UserTier.NON_MEMBER);
    }
}