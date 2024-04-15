package com.ime.lockmanager.user.application.service;

import com.ime.lockmanager.common.format.exception.major.majordetail.NotFoundMajorDetailException;
import com.ime.lockmanager.common.format.exception.user.NotFoundUserException;
import com.ime.lockmanager.locker.domain.lockerdetail.LockerDetail;
import com.ime.lockmanager.major.application.port.out.major.MajorQueryPort;
import com.ime.lockmanager.major.domain.Major;
import com.ime.lockmanager.reservation.application.port.out.ReservationQueryPort;
import com.ime.lockmanager.reservation.domain.Reservation;
import com.ime.lockmanager.user.application.port.in.UserQueryUseCase;
import com.ime.lockmanager.user.application.port.in.req.FindAllUserRequestDto;
import com.ime.lockmanager.user.application.port.in.req.UserInfoRequestDto;
import com.ime.lockmanager.user.application.port.in.res.ApplyingStudentsDto;
import com.ime.lockmanager.user.application.port.in.res.PagingApplyStudentsResponseDto;
import com.ime.lockmanager.user.application.port.out.UserQueryPort;
import com.ime.lockmanager.user.application.port.out.res.AllUserInfoForAdminResponseDto;
import com.ime.lockmanager.user.application.port.out.res.UserInfoQueryResponseDto;
import com.ime.lockmanager.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserQueryService implements UserQueryUseCase {
    private final MajorQueryPort majorQueryPort;
    private final UserQueryPort userQueryPort;
    private final ReservationQueryPort reservationQueryPort;
    private final int PAGE_SIZE = 30;

    @Override
    public PagingApplyStudentsResponseDto findApplyStudentsInMajorByPage(String studentNum, int page) {
        User user = userQueryPort.findByStudentNumWithMajorDetailAndMajor(studentNum)
                .orElseThrow(NotFoundUserException::new);
        Major major = user.getMajorDetail().getMajor();
        return getApplyStudentsResponsePageDto(page, major);
    }

    private PagingApplyStudentsResponseDto getApplyStudentsResponsePageDto(int page, Major major) {
        Page<User> membershipApplicants = userQueryPort
                .findApplicantsByMajorOrderByStudentNumAsc(major, PageRequest.of(page, PAGE_SIZE));
        List<ApplyingStudentsDto> applicantInfos = membershipApplicants.stream()
                .map(applicant ->
                        ApplyingStudentsDto.builder()
                                .studentName(applicant.getName())
                                .studentNum(applicant.getStudentNum())
                                .build()
                ).collect(Collectors.toList());
        return PagingApplyStudentsResponseDto.builder()
                .currentPage(membershipApplicants.getNumber())
                .totalPage(membershipApplicants.getTotalPages())
                .applicant(applicantInfos).build();
    }
    @Override
    public UserInfoQueryResponseDto findUserInfoByStudentNum(UserInfoRequestDto userRequestDto) {
        User user = userQueryPort.findByIdWithMajorDetailAndMajor(userRequestDto.getUserId())
                .orElseThrow(NotFoundUserException::new);
        Optional<Reservation> reservation = getReservationByUserId(user);
        UserInfoQueryResponseDto extracted = buildUserInfoResponse(user, reservation);
        return extracted;
    }

    @Override
    public Page<AllUserInfoForAdminResponseDto> findAllUserInfo(FindAllUserRequestDto requestDto) {
        Major major = majorQueryPort.findById(requestDto.getMajorId())
                .orElseThrow(NotFoundMajorDetailException::new);//예외 따로 처리해야함
        PageRequest pageRequest = PageRequest.of(requestDto.getPage(), PAGE_SIZE);
        Page<User> allUser = userQueryPort
                .pagingByMajorASC(major, requestDto.getSearch(), pageRequest);
        return pagingUsers(allUser);
    }

    private UserInfoQueryResponseDto buildUserInfoResponse(User user, Optional<Reservation> maybeReservation) {
        UserInfoQueryResponseDto.UserInfoQueryResponseDtoBuilder userInfoBuilder = UserInfoQueryResponseDto.builder()
                .name(user.getName())
                .studentNum(user.getStudentNum())
                .userTier(user.getUserTier())
                .userState(user.getUserState())
                .majorDetail(user.getMajorDetail().getName());

        setReservation(maybeReservation, userInfoBuilder);

        return userInfoBuilder.build();
    }

    private void setReservation(Optional<Reservation> maybeReservation, UserInfoQueryResponseDto.UserInfoQueryResponseDtoBuilder userInfoBuilder) {
        maybeReservation.ifPresent(reservation -> {
            LockerDetail lockerDetail = reservation.getLockerDetail();
            userInfoBuilder
                    .lockerName(lockerDetail.getLocker().getName())
                    .lockerDetailNum(lockerDetail.getLockerNum())
                    .lockerDetailId(lockerDetail.getId());
        });
    }

    private PageImpl<AllUserInfoForAdminResponseDto> pagingUsers(Page<User> allUser) {
        PageRequest pageRequest = PageRequest.of(allUser.getNumber(), allUser.getSize());

        Page<AllUserInfoForAdminResponseDto> userPageList = allUser.map(user -> {
            Optional<Reservation> reservation = getReservationByUserId(user);
            return AllUserInfoForAdminResponseDto.of(user, reservation);
        });

        return new PageImpl<>(userPageList.toList(), pageRequest, userPageList.getSize());
    }


    private Optional<Reservation> getReservationByUserId(User user) {
        return reservationQueryPort.findByUserId(user.getId());
    }
}
