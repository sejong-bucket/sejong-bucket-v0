package com.ime.lockmanager.locker.domain.lockerdetail;

import com.ime.lockmanager.common.domain.BaseTimeEntity;
import com.ime.lockmanager.locker.domain.locker.Locker;
import com.ime.lockmanager.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

import static com.ime.lockmanager.locker.domain.lockerdetail.LockerDetailStatus.NON_RESERVED;
import static com.ime.lockmanager.locker.domain.lockerdetail.LockerDetailStatus.RESERVED;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@Entity(name = "LOCKER_DETAIL_TABLE")
public class LockerDetail extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String rowNum;
    private String columnNum;
    private String lockerNum;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "locker_id")
    private Locker locker;


    public Long cancel(){
        this.user.cancelRegister();
        this.user = null;
        return this.id;
    }

    public void register(User user){
        this.user = user;
        user.registerLockerDetail(this);
    }

    public boolean isReserve(){
        return this.user != null;
    }

    public LockerDetailStatus getLockerStatus(){
        if(isReserve()){
            return RESERVED;
        }
        return NON_RESERVED;
    }
}
