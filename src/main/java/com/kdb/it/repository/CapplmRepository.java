package com.kdb.it.repository;

import com.kdb.it.domain.entity.Capplm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 * 신청서 마스터(Capplm) 데이터 접근 리포지토리
 *
 * <p>Spring Data JPA의 {@link JpaRepository}를 상속하여
 * 신청서 마스터 테이블(TAAABB_CAPPLM)에 대한 CRUD 기능을 제공합니다.</p>
 *
 * <p>기본키 타입: {@link String} (apfMngNo: 신청서관리번호)</p>
 *
 * <p>기본 제공 메서드 ({@link JpaRepository} 상속):</p>
 * <ul>
 *   <li>{@code findById(apfMngNo)}: 신청서 관리번호로 단건 조회</li>
 *   <li>{@code findAll()}: 전체 신청서 조회</li>
 *   <li>{@code save(capplm)}: 신청서 저장 (신규 생성 및 수정)</li>
 *   <li>{@code deleteById(apfMngNo)}: 신청서 삭제</li>
 * </ul>
 */
public interface CapplmRepository extends JpaRepository<Capplm, String> {

    /**
     * Oracle 시퀀스(S_APF) 다음 값 조회
     *
     * <p>신청서 생성 시 신청서관리번호(APF_MNG_NO) 채번에 사용합니다.
     * 형식: {@code APF_{연도}{String.format("%08d", seq)}}
     * 예: {@code APF_202600000001}</p>
     *
     * <p>Oracle DB 전용 Native Query입니다.</p>
     *
     * @return Oracle 시퀀스(S_APF)의 다음 값 (Long)
     */
    @Query(value = "SELECT S_APF.NEXTVAL FROM DUAL", nativeQuery = true)
    Long getNextVal();
}
