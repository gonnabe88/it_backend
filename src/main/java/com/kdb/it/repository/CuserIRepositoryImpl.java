package com.kdb.it.repository;

import com.kdb.it.domain.entity.CuserI;
import com.kdb.it.domain.entity.QCuserI;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class CuserIRepositoryImpl implements CuserIRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<CuserI> searchByName(String name) {
        QCuserI cuserI = QCuserI.cuserI;

        return queryFactory.selectFrom(cuserI)
                .where(cuserI.usrNm.contains(name)) // Example: contains search
                .fetch();
    }
}
