package com.ime.lockmanager.major.application.port.in;

import com.ime.lockmanager.common.format.exception.major.majordetail.DuplicatedMajorDetailException;
import com.ime.lockmanager.major.application.port.in.res.CreateMajorResponseDto;
import com.ime.lockmanager.major.application.port.in.res.MajorDetailInMajorResponseDto;
import com.ime.lockmanager.major.application.port.out.majordetail.MajorDetailQueryPort;
import com.ime.lockmanager.major.application.port.out.major.MajorQueryPort;
import com.ime.lockmanager.major.domain.Major;
import com.ime.lockmanager.major.domain.MajorDetail;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
@ActiveProfiles("test")
@SpringBootTest
class MajorDetailUseCaseTest {

    @Autowired
    MajorDetailUseCase majorDetailUseCase;
    @Autowired
    MajorDetailQueryPort majorDetailQueryPort;
    @Autowired
    MajorUseCase majorUseCase;
    @Autowired
    MajorQueryPort majorQueryPort;

    @DisplayName("소학과 생성")
    @Test
    void createMajorDetail() {
        //given
        CreateMajorResponseDto createMajorResponseDto = createMajor();
        Optional<Major> major = majorQueryPort.findByName(createMajorResponseDto.getMajorName());
        CreateMajorDetailRequestDto majorDetailRequestDto = CreateMajorDetailRequestDto.of("지능기전공학과", major.get().getId());
        //when
        majorDetailUseCase.createMajorDetail(majorDetailRequestDto);
        //then
        Optional<MajorDetail> majorDetail = majorDetailQueryPort.findByNameWithMajor(majorDetailRequestDto.getMajorDetailName());
        assertAll(
                () -> assertEquals(majorDetail.get().getName(), majorDetailRequestDto.getMajorDetailName()),
                () -> assertEquals(majorDetail.get().getMajor().getName(), createMajorResponseDto.getMajorName())
        );
    }

    @DisplayName("중복된 소학과명이 존재할시 예외 테스트")
    @Test
    void duplicateMajorDetailNameTest() {
        //given
        CreateMajorResponseDto createMajorResponseDto = createMajor();
        Optional<Major> major = majorQueryPort.findByName(createMajorResponseDto.getMajorName());
        CreateMajorDetailRequestDto majorDetailRequestDto = CreateMajorDetailRequestDto.of("지능기전공학부", major.get().getId());
        //when
        //then
        assertThrows(DuplicatedMajorDetailException.class, () -> majorDetailUseCase.createMajorDetail(majorDetailRequestDto));
    }

    @DisplayName("학과 생성했을때 학과의 소학과 조회")
    @Test
    void findAllByMajorIdAtCreateMajor() {
        //given
        CreateMajorResponseDto createMajorResponseDto = createMajor();
        //when
        List<MajorDetailInMajorResponseDto> responseDtos = majorDetailUseCase.findAllByMajorId(createMajorResponseDto.getMajorId());
        //then
        assertAll(() -> assertTrue(responseDtos.size() == 1),
                () -> assertTrue(responseDtos.stream()
                        .filter(majorDetailInMajorResponseDto ->
                                majorDetailInMajorResponseDto.getMajorDetailName()
                                        .equals(createMajorResponseDto.getMajorName()))
                        .collect(Collectors.toList())
                        .size() == 1)
        );
    }

    @Test
    void 학과내_여러_소학과가_존재할때_조회() {
        //given
        CreateMajorResponseDto createMajorResponseDto = createMajor();
        majorDetailUseCase.createMajorDetail(CreateMajorDetailRequestDto
                .of("지능기전공학과", createMajorResponseDto.getMajorId()));
        //when
        List<MajorDetailInMajorResponseDto> responseDtos = majorDetailUseCase.findAllByMajorId(createMajorResponseDto.getMajorId());
        //then
        assertAll(() -> assertTrue(responseDtos.size() == 2));
    }


    private CreateMajorResponseDto createMajor() {
        CreateMajorRequestDto createMajorRequestDto = CreateMajorRequestDto.of("지능기전공학부");
        return majorUseCase.createMajor(createMajorRequestDto);
    }
}