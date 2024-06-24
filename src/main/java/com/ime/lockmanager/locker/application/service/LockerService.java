package com.ime.lockmanager.locker.application.service;

import com.ime.lockmanager.common.format.exception.major.majordetail.NotFoundMajorDetailException;
import com.ime.lockmanager.locker.adapter.in.res.LockersInfoInMajorResponse;
import com.ime.lockmanager.locker.adapter.in.res.dto.LockersInfoDto;
import com.ime.lockmanager.locker.adapter.in.res.dto.LockersInfoInMajorDto;
import com.ime.lockmanager.locker.application.port.in.LockerDetailUseCase;
import com.ime.lockmanager.locker.application.port.in.LockerUseCase;
import com.ime.lockmanager.locker.application.port.in.dto.CreateLockerDetailDto;
import com.ime.lockmanager.locker.application.port.in.dto.CreatedLockerInfo;
import com.ime.lockmanager.locker.application.port.in.req.FindAllLockerInMajorRequestDto;
import com.ime.lockmanager.locker.application.port.in.req.LockerCreateRequestDto;
import com.ime.lockmanager.locker.application.port.in.res.LeftLockerResponseDto;
import com.ime.lockmanager.locker.application.port.in.res.LockerCreateResponseDto;
import com.ime.lockmanager.locker.application.port.out.LockerCommandPort;
import com.ime.lockmanager.locker.application.port.out.LockerDetailQueryPort;
import com.ime.lockmanager.locker.application.port.out.LockerQueryPort;
import com.ime.lockmanager.locker.domain.locker.Locker;
import com.ime.lockmanager.locker.domain.lockerdetail.LockerDetail;
import com.ime.lockmanager.locker.domain.lockerdetail.LockerDetailStatus;
import com.ime.lockmanager.locker.domain.lockerdetail.dto.LockerDetailInfo;
import com.ime.lockmanager.major.application.port.out.major.MajorQueryPort;
import com.ime.lockmanager.major.domain.Major;
import com.ime.lockmanager.user.application.port.out.UserQueryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
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
                        .build())
                .collect(Collectors.toList());
        return LeftLockerResponseDto.builder()
                .createdLockerInfo(createdLockerInfos)
                .build();
    }

    @Override
    public LockersInfoInMajorResponse findAllLockerInMajor(FindAllLockerInMajorRequestDto requestDto) {
        log.info("사물함 전체 조회 --> 시작");
        Major major = majorQueryPort.findById(requestDto.getMajorId()).orElseThrow(NotFoundMajorDetailException::new);

        List<Locker> lockerByUserMajor = lockerQueryPort.findLockerByUserMajor(major);

        log.info("사물함 전체 조회 --> 끝");
        return LockersInfoInMajorResponse.builder()
                .lockersInfo(
                        lockerByUserMajor.stream()
                                .map(locker -> LockersInfoDto.builder()
                                        .lockerDetail(getLockerDetailInfos(locker))
                                        .locker(getLockersInfoInMajorDto(locker))
                                        .build()
                                ).collect(Collectors.toList()))
                .build();
    }

    private List<LockerDetailInfo> getLockerDetailInfos(Locker locker) {
        return lockerDetailQueryPort.findByLockerId(locker.getId()).stream()
                .map(this::getLockerDetailInfo)
                .collect(Collectors.toList());
    }

    private LockerDetailInfo getLockerDetailInfo(LockerDetail lockerDetail) {
        return LockerDetailInfo.builder()
                .lockerNum(lockerDetail.getLockerNum())
                .status(lockerDetail.getLockerStatus())
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
                .permitStates(locker.getPermitUserState().getName())
                .image(locker.getImageUrl())
                .build();
    }


    @Override
    public LockerCreateResponseDto createLocker(LockerCreateRequestDto requestDto, Long majorId) throws IOException {
        Major userMajor = majorQueryPort.findById(majorId)
                .orElseThrow(NotFoundMajorDetailException::new);//에러 새로 만들어야함

        Locker createdLocker = Locker.createLocker(requestDto.toLockerCreateDto(userMajor));

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
