package com.ime.lockmanager.file.application.service;

import com.ime.lockmanager.common.format.exception.file.InValidCheckingException;
import com.ime.lockmanager.common.format.exception.file.NotValidExcelFormatException;
import com.ime.lockmanager.common.format.exception.user.NotFoundUserException;
import com.ime.lockmanager.file.application.port.in.usecase.MembershipFileAdminUseCase;
import com.ime.lockmanager.major.domain.MajorDetail;
import com.ime.lockmanager.user.application.port.in.UserCommandUseCase;
import com.ime.lockmanager.user.application.port.in.dto.UpdateUserDueInfoDto;
import com.ime.lockmanager.user.application.port.out.UserQueryPort;
import com.ime.lockmanager.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Transactional
@Service
@RequiredArgsConstructor
public class MembershipFileAdminService implements MembershipFileAdminUseCase {
    private final UserCommandUseCase userCommandUseCase;
    private final UserQueryPort userQueryPort;

    @Override
    public void ParseMembershipExcelForUpdateUserDuesInfo(MultipartFile membershipFile, Long userId) throws Exception {
        try (InputStream inputStream = membershipFile.getInputStream()) {
            Workbook workbook = getWorkbook(inputStream, membershipFile.getOriginalFilename());

            User user = getUserById(userId);
            MajorDetail majorDetail = user.getMajorDetail();
            processExcelData(workbook, majorDetail, user);
        }
    }

    private Workbook getWorkbook(InputStream inputStream, String originalFilename) throws IOException {
        String extension = FilenameUtils.getExtension(originalFilename);
        if (!extension.equals("xlsx") && !extension.equals("xls")) {
            throw new NotValidExcelFormatException();
        }

        return (extension.equals("xlsx")) ? new XSSFWorkbook(inputStream) : new HSSFWorkbook(inputStream);
    }

    private User getUserById(Long userId) {
        return userQueryPort.findByIdWithMajorDetailAndMajor(userId)
                .orElseThrow(NotFoundUserException::new);
    }


    private void processExcelData(Workbook workbook, MajorDetail majorDetail, User user) throws Exception {
        List<UpdateUserDueInfoDto> updateUserDueInfoList = IntStream.range(0, workbook.getNumberOfSheets())
                .parallel()
                .mapToObj(workbook::getSheetAt)
                .flatMap(sheet -> IntStream.range(1, sheet.getPhysicalNumberOfRows())
                        .parallel()
                        .mapToObj(sheet::getRow))
                .filter(row -> row != null && row.getCell(0) != null && row.getCell(1) != null && row.getCell(2) != null)
                .map(row -> {
                    synchronized (this) { // Ensure thread safety if you have any shared resources
                        changeRowTypeToString(row);

                        String studentNum = row.getCell(0).getStringCellValue();
                        String studentName = row.getCell(1).getStringCellValue();
                        String checkDues = row.getCell(2).getStringCellValue();

                        // 빈칸이 있는 것은 에러를 뱉고 싶어서 조건을 위에다 씀
                        checkBlank(studentNum, studentName, checkDues);

                        if (studentName.length() == 0 && studentNum.length() == 0 && checkDues.length() == 0) {
                            return null; // Return null for empty case
                        }

                        boolean isDues = determineDuesStatus(checkDues);

                        return UpdateUserDueInfoDto.builder()
                                .isDue(isDues)
                                .studentNum(studentNum)
                                .name(studentName)
                                .majorDetail(majorDetail)
                                .build();
                    }
                })
                .filter(Objects::nonNull) // Filter out null values for empty cases
                .collect(Collectors.toList());
        userCommandUseCase.updateUserDueInfoOrSave(updateUserDueInfoList);
    }

    private void checkBlank(String studentNum, String studentName, String checkDues) {
        if (studentName.length() == 0 || studentNum.length() == 0 || checkDues.length() == 0) {
            throw new InValidCheckingException();
        }
    }

    private void changeRowTypeToString(Row row) {
        row.getCell(0).setCellType(CellType.STRING);
        row.getCell(1).setCellType(CellType.STRING);
        row.getCell(2).setCellType(CellType.STRING);
    }


    private boolean determineDuesStatus(String checkDues) {
        switch (checkDues.toLowerCase()) {
            case "o":
            case "0":
            case "O":
                return true;
            case "x":
            case "X":
                return false;
            default:
                throw new InValidCheckingException();
        }
    }

}
