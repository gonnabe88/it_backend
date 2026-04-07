package com.kdb.it.domain.council.service;

import com.kdb.it.domain.council.dto.CouncilDto;
import com.kdb.it.domain.council.entity.Brsltm;
import com.kdb.it.domain.council.repository.ResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 협의회 결과서 서비스 (Step 3 — 결과서 작성/검토)
 *
 * <p>IT관리자(ITPAD001)가 평가의견을 취합하여 결과서를 작성하고,
 * 작성 완료 시 상태를 RESULT_WRITING으로 전이합니다.</p>
 *
 * <p>결과서 구조:</p>
 * <ul>
 *   <li>1page: 일정공지 정보 (사업명, 심의유형, 일시·장소·위원목록 — BASCTM/BCMMTM에서 조회)</li>
 *   <li>2page: 점검항목별 평균점수 + 종합의견(SYN_OPNN) + 타당성검토의견(CKG_OPNN)</li>
 * </ul>
 *
 * <p>상태 전이 흐름:</p>
 * <pre>
 *   EVALUATING
 *     │  IT관리자가 결과서 저장 (POST /result)
 *     ↓
 *   RESULT_WRITING
 *     │  IT관리자가 결과서 확정 (PUT /result/confirm)
 *     ↓
 *   RESULT_REVIEW
 * </pre>
 *
 * <p>Design Ref: §2.1 ResultService — Step 3 담당</p>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ResultService {

    /** 결과서 리포지토리 (TAAABB_BRSLTM) */
    private final ResultRepository resultRepository;

    /** 협의회 기본 서비스 — 상태 전이용 */
    private final CouncilService councilService;

    /** 평가의견 서비스 — 항목별 평균점수 조회용 */
    private final EvaluationService evaluationService;

    // =========================================================================
    // 조회
    // =========================================================================

    /**
     * 결과서 조회 (IT관리자)
     *
     * <p>결과서 내용(종합의견, 타당성검토의견, 첨부파일)과
     * 점검항목별 평균점수를 함께 반환합니다.
     * 아직 작성 전이면 avgScores만 채워진 빈 결과서를 반환합니다.</p>
     *
     * @param asctId 협의회ID
     * @return 결과서 내용 + 항목별 평균점수
     */
    public CouncilDto.ResultResponse getResult(String asctId) {
        councilService.findActiveCouncil(asctId);

        // 점검항목별 평균점수 (EvaluationService 위임)
        List<CouncilDto.CheckItemAvgScore> avgScores = evaluationService.buildAvgScores(asctId);

        // 결과서 조회 (아직 작성 전이면 빈 DTO 반환)
        return resultRepository.findByAsctIdAndDelYn(asctId, "N")
                .map(r -> new CouncilDto.ResultResponse(
                        r.getSynOpnn(),
                        r.getCkgOpnn(),
                        r.getFlMngNo(),
                        avgScores
                ))
                .orElse(new CouncilDto.ResultResponse(null, null, null, avgScores));
    }

    // =========================================================================
    // 저장
    // =========================================================================

    /**
     * 결과서 저장 (IT관리자)
     *
     * <p>기존 결과서가 있으면 update, 없으면 신규 INSERT합니다.
     * 최초 저장 시 협의회 상태를 EVALUATING → RESULT_WRITING으로 전이합니다.</p>
     *
     * <p>Plan SC: 이미 RESULT_WRITING 이상이면 상태 전이 skip (중복 전이 방지)</p>
     *
     * @param asctId  협의회ID
     * @param request 결과서 작성/수정 요청 (종합의견, 타당성검토의견, 첨부파일번호)
     */
    @Transactional
    public void saveResult(String asctId, CouncilDto.ResultRequest request) {
        councilService.findActiveCouncil(asctId);

        // upsert: 기존 결과서 있으면 update, 없으면 신규 INSERT
        resultRepository.findByAsctIdAndDelYn(asctId, "N")
                .ifPresentOrElse(
                    // 기존 결과서 업데이트
                    existing -> existing.update(
                            request.synOpnn(), request.ckgOpnn(), request.flMngNo()),
                    // 신규 INSERT
                    () -> {
                        Brsltm result = Brsltm.builder()
                                .asctId(asctId)
                                .synOpnn(request.synOpnn())
                                .ckgOpnn(request.ckgOpnn())
                                .flMngNo(request.flMngNo())
                                .build();
                        resultRepository.save(result);
                    }
                );

        // 협의회 상태 전이: EVALUATING → RESULT_WRITING (최초 저장 시 1회만)
        String currentStatus = councilService.findActiveCouncil(asctId).getAsctSts();
        if ("EVALUATING".equals(currentStatus)) {
            councilService.changeStatus(asctId, "RESULT_WRITING");
        }
    }

    /**
     * 결과서 확정 (IT관리자)
     *
     * <p>작성 완료된 결과서를 확정하고 협의회 상태를 RESULT_REVIEW로 전이합니다.
     * RESULT_REVIEW 단계에서 평가위원들이 결과서를 최종 검토합니다.</p>
     *
     * @param asctId 협의회ID
     * @throws IllegalStateException 결과서가 아직 작성되지 않은 경우
     */
    @Transactional
    public void confirmResult(String asctId) {
        councilService.findActiveCouncil(asctId);

        // 결과서 존재 여부 검증
        resultRepository.findByAsctIdAndDelYn(asctId, "N")
                .orElseThrow(() -> new IllegalStateException(
                    "결과서가 아직 작성되지 않았습니다. 결과서를 먼저 저장해 주세요."));

        // 협의회 상태 전이: RESULT_WRITING → RESULT_REVIEW
        councilService.changeStatus(asctId, "RESULT_REVIEW");
    }
}
