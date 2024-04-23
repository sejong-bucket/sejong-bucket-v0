package com.ime.lockmanager.user.domain;

import com.ime.lockmanager.major.domain.Major;
import com.ime.lockmanager.major.domain.MajorDetail;
import com.ime.lockmanager.user.domain.dto.UpdateUserInfoDto;
import com.ime.lockmanager.util.CreateEntityUtil;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UserTest {
    Major major = CreateEntityUtil.createMajor("testMajor");
    MajorDetail majorDetail = CreateEntityUtil.createMajorDetail("testMajorDetail", major);
    @DisplayName("사용자의 권한을 관리자로 변경")
    @Test
    void changeUserToAdmin() {
        //given
        User member = CreateEntityUtil.createUser(UserState.ATTEND, UserTier.MEMBER, Role.ROLE_USER, "3", "19011721", "test", CreateEntityUtil.createMajorDetail("testMajorDetail", CreateEntityUtil.createMajor("testMajor")));
        //when
        member.changeAdmin(true);
        //then
        Assertions.assertThat(member.getRole()).isEqualTo(Role.ROLE_ADMIN);
    }

    @DisplayName("관리자인 사용자의 권한을 사용자로 변경")
    @Test
    void changeAdminToUser() {
        //given
        User admin = CreateEntityUtil.createUser(UserState.ATTEND, UserTier.MEMBER, Role.ROLE_ADMIN, "3", "19011721", "test", CreateEntityUtil.createMajorDetail("testMajorDetail", CreateEntityUtil.createMajor("testMajor")));
        //when
        admin.changeAdmin(false);
        //then
        Assertions.assertThat(admin.getRole()).isEqualTo(Role.ROLE_USER);
    }

    @DisplayName("사용자의 정보 업데이트")
    @Test
    void updateUserInfo() {
        //given
        User member = CreateEntityUtil.createUser(UserState.ATTEND, UserTier.MEMBER, Role.ROLE_USER, "3", "19011721", "test", CreateEntityUtil.createMajorDetail("testMajorDetail", CreateEntityUtil.createMajor("testMajor")));
        UpdateUserInfoDto updateUserInfoDto = UpdateUserInfoDto.builder()
                .userTier(UserTier.MEMBER)
                .grade("4")
                .auth(true)
                .majorDetail(majorDetail)
                .status(UserState.REST).build();
        //when
        member.updateUserInfo(updateUserInfoDto);
        //then
        org.junit.jupiter.api.Assertions.assertAll(()->member.getGrade().equals(updateUserInfoDto.getGrade()),
                ()->member.getUserTier().equals(updateUserInfoDto.getUserTier()),
                ()->member.getGrade().equals(updateUserInfoDto.getGrade()),
                ()->member.getMajorDetail().equals(updateUserInfoDto.getMajorDetail()),
                ()->member.getUserState().equals(updateUserInfoDto.getStatus()));
    }

    @DisplayName("사용자가 학생회비 납부를 신청한 상태")
    @Test
    void applyMembership(){
        //given
        User member = CreateEntityUtil.createUser(UserState.ATTEND, UserTier.NON_MEMBER, Role.ROLE_USER, "3", "19011721", "test", CreateEntityUtil.createMajorDetail("testMajorDetail", CreateEntityUtil.createMajor("testMajor")));
        //when
        member.applyMembership();
        //then
        org.junit.jupiter.api.Assertions.assertTrue(member.getUserTier().equals(UserTier.APPLICANT));
//        Assertions.assertThat(member.getUserTier()).isEqualTo(UserTier.APPLICANT);
    }

    @DisplayName("사용자의 학생회비 납부신청을 수락")
    @Test
    void approveMember(){
        //given
        User member = CreateEntityUtil.createUser(UserState.ATTEND, UserTier.NON_MEMBER, Role.ROLE_USER, "3", "19011721", "test", CreateEntityUtil.createMajorDetail("testMajorDetail", CreateEntityUtil.createMajor("testMajor")));
        //when
        member.approve();
        //then
        Assertions.assertThat(member.getUserTier()).isEqualTo(UserTier.MEMBER);
    }

    @DisplayName("사용자의 학생회비 납부신청을 거부")
    @Test
    void test(){
        //given
        User member = CreateEntityUtil.createUser(UserState.ATTEND, UserTier.MEMBER, Role.ROLE_USER, "3", "19011721", "test", CreateEntityUtil.createMajorDetail("testMajorDetail", CreateEntityUtil.createMajor("testMajor")));
        //when
        member.deny();
        //then
        Assertions.assertThat(member.getUserTier()).isEqualTo(UserTier.NON_MEMBER);
    }
}