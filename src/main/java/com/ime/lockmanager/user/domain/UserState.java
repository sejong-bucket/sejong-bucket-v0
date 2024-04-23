package com.ime.lockmanager.user.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public enum UserState {
    ATTEND("ATTEND","재학"), REST("REST","휴학"), GRADUATE("GRADUATE","졸업");
    private String name;
    private String krName;

    public static UserState match(String state) {
        if (state.equals(ATTEND.krName)) {
            return ATTEND;
        } else if (state.equals(REST.krName)) {
            return REST;
        }
        return GRADUATE;
    }
}
