package com.kdb.it.repository;

import com.kdb.it.domain.entity.Ccodem;
import com.kdb.it.domain.entity.QCcodem;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 공통코드 QueryDSL 커스텀 리포지토리 구현체
 */
@Repository
@RequiredArgsConstructor
public class CcodemRepositoryImpl implements CcodemRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<Ccodem> findByCdIdWithValidDate(String cdId, LocalDate targetDate) {
        QCcodem qCcodem = QCcodem.ccodem;
        LocalDate effectiveDate = (targetDate != null) ? targetDate : LocalDate.now();

        Ccodem result = queryFactory.selectFrom(qCcodem)
                .where(
                        qCcodem.cdId.eq(cdId),
                        qCcodem.delYn.eq("N"),
                        isValidDate(qCcodem, effectiveDate))
                .fetchOne();

        return Optional.ofNullable(result);
    }

    @Override
    public List<Ccodem> findByCttTpWithValidDate(String cttTp, LocalDate targetDate) {
        QCcodem qCcodem = QCcodem.ccodem;
        LocalDate effectiveDate = (targetDate != null) ? targetDate : LocalDate.now();

        return queryFactory.selectFrom(qCcodem)
                .where(
                        qCcodem.cttTp.eq(cttTp),
                        qCcodem.delYn.eq("N"),
                        isValidDate(qCcodem, effectiveDate))
                .orderBy(qCcodem.cdSqn.asc().nullsLast(), qCcodem.cdId.asc())
                .fetch();
    }

    /**
     * 기준일자(effectiveDate)가 시작일자(sttDt)와 종료일자(endDt) 사이에 존재하는지 검증
     * 각 필드가 null일 경우 검증 패스.
     */
    private BooleanExpression isValidDate(QCcodem ccodem, LocalDate effectiveDate) {
        BooleanExpression afterStartDate = ccodem.sttDt.isNull().or(ccodem.sttDt.loe(effectiveDate));
        BooleanExpression beforeEndDate = ccodem.endDt.isNull().or(ccodem.endDt.goe(effectiveDate));
        return afterStartDate.and(beforeEndDate);
    }
}
