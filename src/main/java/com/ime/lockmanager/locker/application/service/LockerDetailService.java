package com.ime.lockmanager.locker.application.service;


import com.ime.lockmanager.locker.application.port.in.LockerDetailUseCase;
import com.ime.lockmanager.locker.application.port.in.dto.CreateLockerDetailDto;
import com.ime.lockmanager.locker.application.port.out.LockerDetailCommandPort;
import com.ime.lockmanager.locker.domain.locker.Locker;
import com.ime.lockmanager.locker.domain.lockerdetail.LockerDetail;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.ime.lockmanager.locker.adapter.in.req.NumberIncreaseDirection.DOWN;

@Service
@RequiredArgsConstructor
public class LockerDetailService implements LockerDetailUseCase {
    private final LockerDetailCommandPort lockerDetailCommandPort;
    @Override
    public void createLockerDetails(CreateLockerDetailDto createLockerDetailDto, Locker saveLocker) {
        int totalColumns = createLockerDetailDto.getTotalColumn();
        int totalRows = createLockerDetailDto.getTotalRow();
        List<LockerDetail> lockerDetails = null;
        if (createLockerDetailDto.getNumberIncreaseDirection().equals(DOWN)) {
            lockerDetails = makeLockerDetails(saveLocker, totalColumns, totalRows);
        } else {
            lockerDetails = makeLockerDetails(saveLocker, totalRows, totalColumns);
        }
        lockerDetailCommandPort.saveAll(lockerDetails);
    }

    private List<LockerDetail> makeLockerDetails(Locker saveLocker, int master, int slave) {
        List<LockerDetail> saveLockerDetails = new ArrayList<>();

        int num = 0;
        for (int i = 1; i <= master; i++) {
            for (int j = 1; j <= slave; j++) {
                saveLockerDetails.add(getLockerDetail(saveLocker, ++num, i, j));
            }
        }
        return saveLockerDetails;
    }

    private LockerDetail getLockerDetail(Locker saveLocker, int num, int i, int j) {
        return LockerDetail.builder()
                .locker(saveLocker)
                .lockerNum(Integer.toString(num))
                .rowNum(Integer.toString(j))
                .columnNum(Integer.toString(i))
                .build();
    }
}
