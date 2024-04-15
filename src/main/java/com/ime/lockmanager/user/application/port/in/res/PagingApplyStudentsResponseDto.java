package com.ime.lockmanager.user.application.port.in.res;

import com.ime.lockmanager.user.adapter.in.res.AllApplyingStudentPageResponse;
import lombok.Builder;

import java.util.List;

@Builder
public class PagingApplyStudentsResponseDto {
    private int currentPage;
    private int totalPage;
    private List<ApplyingStudentsDto> applicant;
    public AllApplyingStudentPageResponse toResponse(){
        return AllApplyingStudentPageResponse.builder()
                .applicant(applicant)
                .currentPage(currentPage)
                .totalPage(totalPage)
                .build();
    }
}
