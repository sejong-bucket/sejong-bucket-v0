package com.ime.lockmanager.locker.adapter.out;

import com.ime.lockmanager.locker.domain.lockerdetail.LockerDetail;
import com.ime.lockmanager.major.domain.Major;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LockerDetailJpaRepository extends JpaRepository<LockerDetail,Long> {

    @Query("SELECT LD FROM LOCKER_DETAIL_TABLE LD JOIN FETCH LD.locker L WHERE LD.id = :lockerDetailId")
    Optional<LockerDetail> findByIdWithLocker(@Param("lockerDetailId")Long lockerDetailId);

    @Query("SELECT LD FROM LOCKER_DETAIL_TABLE LD ")
    List<LockerDetail> findLockerDetailByMajor(Major major);


    List<LockerDetail> findByLockerId(Long lockerId);
}
