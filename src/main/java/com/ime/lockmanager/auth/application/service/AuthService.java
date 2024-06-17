package com.ime.lockmanager.auth.application.service;

import com.ime.lockmanager.auth.application.port.in.req.LoginRequestDto;
import com.ime.lockmanager.auth.application.port.in.res.LoginTokenResponseDto;
import com.ime.lockmanager.auth.application.port.in.res.ReissueTokenResponseDto;
import com.ime.lockmanager.auth.application.port.in.usecase.AuthUseCase;
import com.ime.lockmanager.auth.application.port.out.AuthToRedisQueryPort;
import com.ime.lockmanager.auth.application.port.out.AuthToUserCommandPort;
import com.ime.lockmanager.auth.application.port.out.AuthToUserQueryPort;
import com.ime.lockmanager.auth.application.service.dto.LoginInfoDto;
import com.ime.lockmanager.auth.domain.AuthUser;
import com.ime.lockmanager.common.aop.meta.DistributeLock;
import com.ime.lockmanager.common.format.exception.auth.jwt.InvalidRefreshTokenException;
import com.ime.lockmanager.common.format.exception.major.majordetail.NotFoundMajorDetailException;
import com.ime.lockmanager.common.format.exception.user.NotFoundUserException;
import com.ime.lockmanager.common.jwt.JwtHeaderUtil;
import com.ime.lockmanager.common.jwt.JwtProvider;
import com.ime.lockmanager.common.jwt.TokenSet;
import com.ime.lockmanager.common.webclient.sejong.service.dto.res.SejongMemberResponseDto;
import com.ime.lockmanager.common.webclient.sejong.service.SejongLoginService;
import com.ime.lockmanager.major.application.port.out.majordetail.MajorDetailQueryPort;
import com.ime.lockmanager.major.domain.Major;
import com.ime.lockmanager.major.domain.MajorDetail;
import com.ime.lockmanager.user.domain.User;
import com.ime.lockmanager.user.domain.UserState;
import com.ime.lockmanager.user.domain.UserTier;
import com.ime.lockmanager.user.domain.dto.UpdateUserInfoDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.Duration;
import java.util.Objects;

import static com.ime.lockmanager.user.domain.Role.ROLE_USER;
import static com.ime.lockmanager.user.domain.UserTier.MEMBER;

@Transactional
@RequiredArgsConstructor
@Service
class AuthService implements AuthUseCase {
    private static final String USER_KEY = "USER_";
    private final AuthToUserQueryPort authToUserQueryPort;
    private final AuthToUserCommandPort authToUserCommandPort;
    private final JwtProvider jwtProvider;
    private final AuthToRedisQueryPort authToRedisQueryPort;
    private final SejongLoginService sejongLoginService;
    private final MajorDetailQueryPort majorDetailQueryPort;

    @DistributeLock(identifier = USER_KEY, key = "#dto.id")
    @Override
    public LoginTokenResponseDto login(LoginRequestDto dto) {
        // 1. 학교에 로그인하여 정보 받아옴
        SejongMemberResponseDto userInfoInSejong = sejongLoginService.callSejongLoginApi(dto.toSejongMemberDto());
        // 2. 서비스에 가입된 학과인지 검색
        MajorDetail majorDetail = majorDetailQueryPort.findByNameWithMajor(getMajorDetailName(userInfoInSejong))
                .orElseThrow(() -> new NotFoundMajorDetailException());
        Major major = majorDetail.getMajor();

        // 저장된 사용자의 정보를 또회 또는 저장
        User user = saveOrFindUser(majorDetail, dto, userInfoInSejong);

        // 기존의 저장된 사용자의 경우 재학상태가 바꼈다면 업데이트
        updateUserInfo(userInfoInSejong, majorDetail, user);

        // 토큰 생성
        TokenSet tokenSet = jwtProvider.makeToken(user);

        return LoginTokenResponseDto.of(tokenSet.getAccessToken(),
                tokenSet.getRefreshToken(),
                user.getRole(),
                user.getId(),
                major.getId(),
                major.getName());
    }

    private User saveOrFindUser(MajorDetail majorDetail,
                                LoginRequestDto loginRequestDto,
                                SejongMemberResponseDto sejongMemberResponseDto) {
        return authToUserQueryPort.findByStudentNum(loginRequestDto.getId()).orElseGet(() ->
                authToUserCommandPort.save(createUserByLoginInfo(
                        LoginInfoDto.builder()
                                .grade(sejongMemberResponseDto.getResult().getBody().getGrade())
                                .majorDetail(majorDetail)
                                .name(sejongMemberResponseDto.getResult().getBody().getName())
                                .status(sejongMemberResponseDto.getResult().getBody().getStatus())
                                .studentNum(loginRequestDto.getId())
                                .build())
                ));
    }

    /**
     * Todo
     * 로그인할때 업데이트니까 userTier업데이트는 없애도 되지 않을까?
     */
    private void updateUserInfo(SejongMemberResponseDto sejongMemberResponseDto, MajorDetail majorDetail, User user) {
        UserState matchUserState = UserState.match(sejongMemberResponseDto.getResult().getBody().getStatus());
        user.updateUserInfo(UpdateUserInfoDto.builder()
                .userTier(MEMBER)
                .auth(true)
                .status(matchUserState)
                .grade(sejongMemberResponseDto.getResult().getBody().getGrade())
                .majorDetail(majorDetail)
                .build());
    }

    private String getMajorDetailName(SejongMemberResponseDto sejongMemberResponseDto) {
        return sejongMemberResponseDto.getResult().getBody().getMajor();
    }

    private User createUserByLoginInfo(LoginInfoDto loginInfoDto) {
        UserState matchUserState = UserState.match(loginInfoDto.getStatus());

        return User.builder()
                .userTier(MEMBER)
                .name(loginInfoDto.getName())
                .userState(matchUserState)
                .studentNum(loginInfoDto.getStudentNum())
                .role(ROLE_USER)
                .auth(true)
                .grade(loginInfoDto.getGrade())
                .majorDetail(loginInfoDto.getMajorDetail())
                .build();
    }


    @Override
    public ReissueTokenResponseDto reissue(String refreshToken) {
        String bearerToken = JwtHeaderUtil.getBearerToken(refreshToken);
        String studentNum = (String) jwtProvider.convertAuthToken(bearerToken).getTokenClaims().get("studentNum");
        String redisRT = authToRedisQueryPort.getRefreshToken(studentNum);

        if (Objects.isNull(redisRT) || !redisRT.equals(refreshToken)) {
            throw new InvalidRefreshTokenException();
        }

        User byStudentNum = authToUserQueryPort.findByStudentNum(studentNum)
                .orElseThrow(NotFoundUserException::new);

        TokenSet tokenSet = jwtProvider.makeToken(byStudentNum);

        authToRedisQueryPort.removeAndSaveRefreshToken(studentNum, JwtHeaderUtil.getBearerToken(tokenSet.getRefreshToken()));

        return ReissueTokenResponseDto.of(tokenSet.getAccessToken(), tokenSet.getRefreshToken());
    }

    @Override
    public void logout(String accessToken) {
        String bearerToken = JwtHeaderUtil.getBearerToken(accessToken);

        Long tokenExpiration = jwtProvider.getTokenExpiration(bearerToken);

        String studentNum = SecurityContextHolder.getContext().getAuthentication().getName();

        if (authToRedisQueryPort.getRefreshToken(studentNum) != null) {
            authToRedisQueryPort.removeRefreshToken(studentNum);
        }

        authToRedisQueryPort.logoutTokenSave(JwtHeaderUtil.getBearerToken(accessToken), Duration.ofMillis(tokenExpiration));
    }
}