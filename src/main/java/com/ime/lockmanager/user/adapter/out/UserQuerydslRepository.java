package com.ime.lockmanager.user.adapter.out;

import com.ime.lockmanager.major.domain.Major;
import com.ime.lockmanager.user.domain.User;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.ime.lockmanager.user.domain.QUser.user;

@Repository
@RequiredArgsConstructor
public class UserQuerydslRepository{
    private final JPAQueryFactory jpaQueryFactory;

    public Page<User> pagingAndSearchUserInMajorASC(Major userMajor, String search, Pageable pageable) {
        BooleanBuilder builder = new BooleanBuilder();
        if(search != null) {
            builder.and(user.name.eq(search).or(user.studentNum.eq(search)));
        }
        List<User> userInfos = jpaQueryFactory.selectFrom(user)
                .join(user.majorDetail.major)
                .orderBy(user.studentNum.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .where(user.majorDetail.major.eq(userMajor))
                .where(builder)
                .fetch();
        int total = jpaQueryFactory
                .selectFrom(user)
                .where(user.majorDetail.major.eq(userMajor))
                .where(builder)
                .fetch().size();
        return new PageImpl<>(userInfos, pageable, total);
    }
}
