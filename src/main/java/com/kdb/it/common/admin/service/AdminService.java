package com.kdb.it.common.admin.service;

import com.kdb.it.common.admin.dto.AdminDto;
import com.kdb.it.common.code.entity.Ccodem;
import com.kdb.it.common.code.repository.CodeRepository;
import com.kdb.it.common.iam.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 관리자 기능 서비스
 *
 * <p>
 * 기존 리포지토리를 DI 받아 관리자 전용 로직을 처리합니다.
 * 엔티티/리포지토리는 신규 생성 없이 기존 패키지를 재사용합니다.
 * </p>
 *
 * <p>
 * 의존 패키지:
 * </p>
 * <ul>
 *   <li>{@code common/code} — 공통코드(Ccodem, CodeRepository)</li>
 *   <li>{@code common/iam} — 사용자(CuserI, UserRepository) — 이름 변환용</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminService {

    // Design Ref: §2.3 — 기존 리포지토리 DI 재사용, 신규 리포지토리 생성 없음
    private final CodeRepository codeRepository;
    private final UserRepository userRepository;

    // =========================================================================
    // 공통코드 (TAAABB_CCODEM)
    // =========================================================================

    /**
     * 삭제되지 않은 전체 공통코드 목록을 조회합니다.
     * 최초생성자·마지막수정자 사원번호를 이름으로 일괄 변환하여 반환합니다.
     *
     * @return 공통코드 응답 DTO 목록
     */
    public List<AdminDto.CodeResponse> getCodes() {
        List<Ccodem> codes = codeRepository.findAllActive();

        // 감사 필드의 고유 ENO를 한 번의 배치 쿼리로 이름 조회 (N+1 방지)
        Set<String> enos = codes.stream()
                .flatMap(c -> Stream.of(c.getFstEnrUsid(), c.getLstChgUsid()))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<String, String> userNameMap = userRepository.findByEnoIn(enos).stream()
                .collect(Collectors.toMap(u -> u.getEno(), u -> u.getUsrNm()));

        return codes.stream()
                .map(c -> toCodeResponse(c, userNameMap))
                .toList();
    }

    /**
     * 신규 공통코드를 추가합니다.
     * C_ID 중복 시 예외를 발생시킵니다.
     *
     * @param req 공통코드 생성 요청 DTO
     * @throws IllegalArgumentException 코드ID 중복 시
     */
    @Transactional
    public void createCode(AdminDto.CodeRequest req) {
        if (codeRepository.existsByCdId(req.cdId())) {
            throw new IllegalArgumentException("이미 존재하는 코드ID입니다: " + req.cdId());
        }
        Ccodem code = Ccodem.builder()
                .cdId(req.cdId())
                .cdNm(req.cdNm())
                .cdva(req.cdva())
                .cdDes(req.cdDes())
                .cttTp(req.cttTp())
                .cttTpDes(req.cttTpDes())
                .sttDt(req.sttDt())
                .endDt(req.endDt())
                .cdSqn(req.cdSqn())
                .build();
        codeRepository.save(code);
    }

    /**
     * 공통코드 정보를 수정합니다.
     * Dirty Checking을 활용하여 별도 save() 호출 없이 변경사항을 반영합니다.
     *
     * @param cdId 코드ID
     * @param req  공통코드 수정 요청 DTO
     * @throws IllegalArgumentException 코드를 찾을 수 없는 경우
     */
    @Transactional
    public void updateCode(String cdId, AdminDto.CodeRequest req) {
        Ccodem code = codeRepository.findByCdIdAndDelYn(cdId, "N")
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 코드ID입니다: " + cdId));
        // Dirty Checking — save() 불필요
        code.update(req.cdNm(), req.cdva(), req.cdDes(), req.cttTp(),
                req.cttTpDes(), req.cdSqn(), req.sttDt(), req.endDt());
    }

    /**
     * 공통코드를 논리 삭제(Soft Delete)합니다.
     * DEL_YN='Y' 처리 — 물리 삭제 금지.
     *
     * @param cdId 코드ID
     * @throws IllegalArgumentException 코드를 찾을 수 없는 경우
     */
    @Transactional
    public void deleteCode(String cdId) {
        // Plan SC: Soft Delete 요구사항 (C-08)
        Ccodem code = codeRepository.findByCdIdAndDelYn(cdId, "N")
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 코드ID입니다: " + cdId));
        code.delete();
    }

    /**
     * Ccodem 엔티티를 CodeResponse DTO로 변환합니다.
     *
     * @param c           공통코드 엔티티
     * @param userNameMap ENO → 사용자명 매핑 (배치 조회 결과)
     */
    private AdminDto.CodeResponse toCodeResponse(Ccodem c, Map<String, String> userNameMap) {
        return new AdminDto.CodeResponse(
                c.getCdId(),
                c.getCdNm(),
                c.getCdva(),
                c.getCdDes(),
                c.getCttTp(),
                c.getCttTpDes(),
                c.getSttDt(),
                c.getEndDt(),
                c.getCdSqn(),
                c.getFstEnrDtm(),
                c.getFstEnrUsid(),
                userNameMap.getOrDefault(c.getFstEnrUsid(), c.getFstEnrUsid()),
                c.getLstChgDtm(),
                c.getLstChgUsid(),
                userNameMap.getOrDefault(c.getLstChgUsid(), c.getLstChgUsid())
        );
    }

    // =========================================================================
    // 자격등급, 사용자, 조직, 역할, 로그인이력, JWT토큰, 첨부파일, 대시보드
    // — Session 2/3에서 구현 예정
    // =========================================================================
}
