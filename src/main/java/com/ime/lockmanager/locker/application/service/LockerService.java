package com.ime.lockmanager.locker.application.service;

import com.ime.lockmanager.common.format.exception.locker.NotFoundLockerException;
import com.ime.lockmanager.common.format.exception.major.majordetail.NotFoundMajorDetailException;
import com.ime.lockmanager.common.format.exception.user.NotFoundUserException;
import com.ime.lockmanager.locker.adapter.in.res.LockersInfoInMajorResponse;
import com.ime.lockmanager.locker.adapter.in.res.dto.LockersInfoDto;
import com.ime.lockmanager.locker.adapter.in.res.dto.LockersInfoInMajorDto;
import com.ime.lockmanager.locker.application.port.in.LockerDetailUseCase;
import com.ime.lockmanager.locker.application.port.in.LockerUseCase;
import com.ime.lockmanager.locker.application.port.in.dto.CreateLockerDetailDto;
import com.ime.lockmanager.locker.application.port.in.dto.CreatedLockerInfo;
import com.ime.lockmanager.locker.application.port.in.req.FindAllLockerInMajorRequestDto;
import com.ime.lockmanager.locker.application.port.in.req.LockerCreateRequestDto;
import com.ime.lockmanager.locker.application.port.in.req.ModifyLockerInfoReqeustDto;
import com.ime.lockmanager.locker.application.port.in.res.LeftLockerResponseDto;
import com.ime.lockmanager.locker.application.port.in.res.LockerCreateResponseDto;
import com.ime.lockmanager.locker.application.port.out.LockerCommandPort;
import com.ime.lockmanager.locker.application.port.out.LockerDetailQueryPort;
import com.ime.lockmanager.locker.application.port.out.LockerQueryPort;
import com.ime.lockmanager.locker.domain.Period;
import com.ime.lockmanager.locker.domain.locker.Locker;
import com.ime.lockmanager.locker.domain.lockerdetail.LockerDetail;
import com.ime.lockmanager.locker.domain.lockerdetail.dto.LockerDetailInfo;
import com.ime.lockmanager.major.application.port.out.major.MajorQueryPort;
import com.ime.lockmanager.major.domain.Major;
import com.ime.lockmanager.user.application.port.out.UserQueryPort;
import com.ime.lockmanager.user.domain.User;
import com.ime.lockmanager.user.domain.UserState;
import com.ime.lockmanager.user.domain.UserTier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@Transactional
@RequiredArgsConstructor
@Service
class LockerService implements LockerUseCase {
    private final LockerQueryPort lockerQueryPort;
    private final LockerDetailUseCase lockerDetailUseCase;
    private final MajorQueryPort majorQueryPort;
    private final UserQueryPort userQueryPort;
//    private final ImageFileAdminService imageFileAdminService;
    private final LockerCommandPort lockerCommandPort;
    private final LockerDetailQueryPort lockerDetailQueryPort;


    //남은 사물함 목록
    @Override
    public LeftLockerResponseDto getCreatedLockers(Long majorId) {
        List<CreatedLockerInfo> createdLockerInfos = lockerQueryPort.findByMajorId(majorId).stream()
                .map(locker -> CreatedLockerInfo.builder()
                        .id(locker.getId())
                        .startReservationTime(locker.getPeriod().getStartDateTime())
                        .endReservationTime(locker.getPeriod().getEndDateTime())
                        .totalColumn(locker.getTotalColumn())
                        .totalRow(locker.getTotalRow())
                        .permitStates(locker.getPermitUserState())
                        .image(locker.getImageUrl())
                        .name(locker.getName())
                        .permitTiers(locker.getPermitUserTier())
                        .build())
                .collect(Collectors.toList());
        return LeftLockerResponseDto.builder()
                .createdLockerInfo(createdLockerInfos)
                .build();
    }


    @Override
    public void modifyLockerInfo(ModifyLockerInfoReqeustDto reqeustDto) throws IOException {
        Locker locker = lockerQueryPort.findByLockerId(reqeustDto.getLockerId()).orElseThrow(NotFoundLockerException::new);

//        changeImage(reqeustDto.getImage(), locker);

        changeReservationTime(reqeustDto.getStartTime(), reqeustDto.getEndTime(), locker);

        changeUserStatesCondition(reqeustDto.getUserStates(), locker);

        changeUserTierCondition(reqeustDto.getUserTiers(), locker);

        changeLockerName(reqeustDto.getLockerName(), locker);
    }

    private void changeUserTierCondition(List<UserTier> newUserTiers, Locker locker) {
        if (newUserTiers == null || newUserTiers.isEmpty()) {
            return;
        }
        locker.getPermitUserTier().clear();
        newUserTiers.stream().forEach(userTier -> locker.getPermitUserTier().add(userTier));
    }

    private void changeUserStatesCondition(List<UserState> newUserStates, Locker locker) {
        if (newUserStates == null || newUserStates.isEmpty()) {
            return;
        }
        locker.getPermitUserState().clear();
        newUserStates.stream().forEach(userState -> locker.getPermitUserState().add(userState));
    }

    private void changeReservationTime(LocalDateTime newStartTime, LocalDateTime newEndTime, Locker locker) {
        if (newStartTime == null || newEndTime == null) {
            return;
        }
        locker.modifiedDateTime(Period.builder()
                .startDateTime(newStartTime)
                .endDateTime(newEndTime)
                .build());
    }


    private void changeImage(MultipartFile newImage, Locker locker) throws IOException {
        /*if (newImage == null || newImage.isEmpty()) {
            return;
        }
        String originalImageUrl = locker.getImageUrl();
        if (!originalImageUrl.isBlank()) {
            //기존 이미지 삭제
            imageFileAdminService.deleteImageToS3(originalImageUrl);
        }
        String newImageUrl = imageFileAdminService.saveImageToS3(newImage);
        locker.modifiedImageInfo(newImageUrl);*/
    }


    private void changeLockerName(String newLockerName, Locker locker) {
        if (newLockerName == null) {
            return;
        }
        locker.rename(newLockerName);
    }


    @Override
    public LockersInfoInMajorResponse findAllLockerInMajor(FindAllLockerInMajorRequestDto requestDto) {
        log.info("사물함 전체 조회 --> 시작");
        User user = userQueryPort.findByIdWithMajorDetailAndMajor(requestDto.getUserId())
                .orElseThrow(NotFoundUserException::new);

        Major major = user.getMajorDetail().getMajor();

        List<Locker> lockerByUserMajor = lockerQueryPort.findLockerByUserMajor(major);
        log.info("사물함 전체 조회 --> 끝");
        LockersInfoInMajorResponse response = LockersInfoInMajorResponse.builder()
                .lockersInfo(
                        lockerByUserMajor.stream()
                                .map(locker -> LockersInfoDto.builder()
                                        .lockerDetail(getLockerDetailInfos(locker))
                                        .locker(getLockersInfoInMajorDto(locker))
                                        .build()
                                ).collect(Collectors.toList()))
                .build();
        return response;
    }

    private List<LockerDetailInfo> getLockerDetailInfos(Locker locker) {
        return lockerDetailQueryPort.findByLockerId(locker.getId()).stream()
                .map(
                        lockerDetail ->
                                getLockerDetailInfo(lockerDetail)
                ).collect(Collectors.toList());
    }

    private LockerDetailInfo getLockerDetailInfo(LockerDetail lockerDetail) {
        return LockerDetailInfo.builder()
                .lockerNum(lockerDetail.getLockerNum())
                .status(lockerDetail.getLockerDetailStatus())
                .columnNum(lockerDetail.getColumnNum())
                .rowNum(lockerDetail.getRowNum())
                .id(lockerDetail.getId())
                .build();
    }

    private static LockersInfoInMajorDto getLockersInfoInMajorDto(Locker locker) {
        return LockersInfoInMajorDto.builder()
                .id(locker.getId())
                .endReservationTime(locker.getPeriod().getEndDateTime())
                .startReservationTime(locker.getPeriod().getStartDateTime())
                .name(locker.getName())
                .totalColumn(locker.getTotalColumn())
                .totalRow(locker.getTotalRow())
                .permitTiers(locker.getPermitUserTier().stream().map(tier -> tier.getName()).collect(Collectors.toList()))
                .permitStates(locker.getPermitUserState().stream().map(userState -> userState.getName()).collect(Collectors.toList()))
                .image(locker.getImageUrl())
                .build();
    }

    @Override
    public LockerCreateResponseDto createLocker(LockerCreateRequestDto requestDto, Long majorId) throws IOException {
        // 이미지 삽입코드 주석처리
        /*String imageUrl = null;
        if (!requestDto.getImage().isEmpty()) { //이미지가 있을때
            imageUrl = imageFileAdminService.saveImageToS3(requestDto.getImage());
        }*/

        Major userMajor = majorQueryPort.findById(majorId)
                .orElseThrow(NotFoundMajorDetailException::new);//에러 새로 만들어야함

        Locker createdLocker = Locker.createLocker(
                requestDto.toLockerCreateDto(
                        userMajor/*, imageUrl*/
                )
        );

        Locker saveLocker = lockerCommandPort.save(createdLocker);

        lockerDetailUseCase.createLockerDetails(CreateLockerDetailDto.builder()
                        .totalRow(Integer.valueOf(requestDto.getTotalRow()))
                        .totalColumn(Integer.valueOf(requestDto.getTotalColumn()))
                        .numberIncreaseDirection(requestDto.getNumberIncreaseDirection()).build(),
                createdLocker);

        return LockerCreateResponseDto.builder()
                .createdLockerId(saveLocker.getId())
                .createdLockerName(saveLocker.getName())
                .build();

    }
}
