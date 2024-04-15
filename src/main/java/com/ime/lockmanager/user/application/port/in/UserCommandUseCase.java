package com.ime.lockmanager.user.application.port.in;

import com.ime.lockmanager.user.application.port.in.dto.UpdateUserDueInfoDto;
import com.ime.lockmanager.user.application.port.in.req.DetermineApplyingRequestDto;
import com.ime.lockmanager.user.application.port.in.req.ModifiedUserInfoRequestDto;
import com.ime.lockmanager.user.application.port.in.res.UserTierResponseDto;

import java.util.List;

public interface UserCommandUseCase {
    void modifiedUserInfo(ModifiedUserInfoRequestDto requestDto) throws Exception;

    void updateUserDueInfoOrSave(List<UpdateUserDueInfoDto> updateUserDueInfoDto) throws Exception;

    void applyMembership(Long userId);

    UserTierResponseDto determineApplying(DetermineApplyingRequestDto toRequestDto, boolean isApprove);
}
