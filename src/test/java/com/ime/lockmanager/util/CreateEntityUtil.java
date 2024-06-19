package com.ime.lockmanager.util;

import com.ime.lockmanager.major.domain.Major;
import com.ime.lockmanager.major.domain.MajorDetail;
import com.ime.lockmanager.user.domain.Role;
import com.ime.lockmanager.user.domain.User;
import com.ime.lockmanager.user.domain.UserState;

public class CreateEntityUtil {
    public static MajorDetail createMajorDetail(String name, Major major) {
        return MajorDetail.builder().name(name).major(major).build();
    }


    public static Major createMajor(String name) {
        return Major.builder().id(1l).name(name).build();
    }

    public static User createUser(UserState userState,
                                  UserTier userTier,
                                  Role userRole,
                                  String grade,
                                  String studentNum,
                                  String name, MajorDetail majorDetail) {
        return User.builder()
                .name(name)
                .userState(userState)
                .userTier(userTier)
                .auth(true)
                .majorDetail(majorDetail)
                .studentNum(studentNum)
                .id(1l)
                .role(userRole)
                .grade(grade)
                .build();
    }

    public static User createTestUser(UserState userState,
                                      UserTier userTier,
                                      Role userRole,
                                      String studentNum, MajorDetail majorDetail) {
        return createUser(userState, userTier, userRole, "3", studentNum, "test", majorDetail);
    }
}
