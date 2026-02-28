package com.kdb.it.dto;

import com.kdb.it.domain.entity.Capplm;
import com.kdb.it.domain.entity.Cdecim;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 신청서(결재) 관련 DTO 클래스 모음
 *
 * <p>신청서 등록, 결재(단건/일괄), 조회에 사용되는 Request/Response DTO를
 * 정적 중첩 클래스(Static Nested Class) 형태로 관리합니다.</p>
 *
 * <p>포함된 DTO:</p>
 * <ul>
 *   <li>{@link CreateRequest}: 신청서 등록 요청</li>
 *   <li>{@link ApproveRequest}: 단건 결재 요청</li>
 *   <li>{@link BulkApproveRequest}: 일괄 결재 요청</li>
 *   <li>{@link ApprovalItem}: 일괄 결재 요청의 개별 항목</li>
 *   <li>{@link BulkApproveResponse}: 일괄 결재 응답</li>
 *   <li>{@link ApprovalResult}: 일괄 결재 응답의 개별 결과</li>
 *   <li>{@link BulkGetRequest}: 일괄 조회 요청</li>
 *   <li>{@link Response}: 신청서 조회 응답 (결재자 목록 포함)</li>
 *   <li>{@link ApproverResponse}: 결재자 정보 응답</li>
 * </ul>
 */
public class ApplicationDto {

    /**
     * 신청서 등록 요청 DTO
     *
     * <p>신규 신청서를 등록할 때 사용합니다. 원본 데이터 연결 정보와
     * 신청서 본문, 결재자 목록을 포함합니다.</p>
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @Schema(name = "ApplicationCreateRequest")
    public static class CreateRequest {
        /** 원본 테이블코드 (예: "BPRJTM" = 정보화사업, "BCOSTM" = 전산관리비) */
        @Schema(description = "원본 테이블코드")
        private String orcTbCd;

        /** 원본 테이블의 PK값 (예: 프로젝트관리번호 "PRJ-2026-0001") */
        @Schema(description = "원본 테이블의 PK값")
        private String orcPkVl;

        /** 원본 테이블의 SNO(일련번호)값 (예: "1") */
        @Schema(description = "원본 테이블의 sno값")
        private String orcSnoVl;

        /** 신청서명 (예: "2026년 정보화사업 예산 신청") */
        @Schema(description = "신청서명")
        private String apfNm;

        /**
         * 신청서 세부 내용 (JSON 형식)
         * <p>결재선(approvalLine) 정보를 포함하는 JSON 문자열.
         * 결재 처리 시 각 결재자의 date 필드가 업데이트됩니다.</p>
         */
        @Schema(description = "신청서세부내용")
        private String apfDtlCone;

        /** 신청자 사원번호 (현재 로그인한 사용자) */
        @Schema(description = "신청 사원번호")
        private String rqsEno;

        /** 신청의견 (신청 사유, 비고 등) */
        @Schema(description = "신청의견")
        private String rqsOpnn;

        /**
         * 결재자 사원번호 목록 (순서대로)
         * <p>순서대로 결재선을 구성합니다. 동일 사원번호가 연속으로 올 수 있으며,
         * 이 경우 해당 결재자가 한 번 승인하면 연속 항목 모두 승인됩니다.</p>
         */
        @Schema(description = "결재자 사원번호 목록 (순서대로)")
        private List<String> approverEnos;
    }

    /**
     * 단건 결재 요청 DTO
     *
     * <p>특정 신청서에 대해 결재자가 승인 또는 반려할 때 사용합니다.</p>
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @Schema(name = "ApplicationApproveRequest")
    public static class ApproveRequest {
        /**
         * 결재자 사원번호
         * <p>실제 서비스에서는 JWT 토큰에서 추출한 현재 사용자 사번을 사용해야 합니다.
         * 현재는 요청 Body에서 받도록 구현되어 있습니다.</p>
         */
        @Schema(description = "결재자 사원번호 (실제로는 세션에서 가져와야 함)")
        private String dcdEno;

        /** 결재의견 (승인/반려 사유) */
        @Schema(description = "결재의견")
        private String dcdOpnn;

        /** 결재상태 (허용값: "승인" 또는 "반려") */
        @Schema(description = "결재상태 (승인/반려)")
        private String dcdSts;
    }

    /**
     * 일괄 결재 요청 DTO
     *
     * <p>여러 신청서를 한 번에 결재 처리할 때 사용합니다.
     * 하나라도 실패하면 전체 트랜잭션이 롤백됩니다.</p>
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @Schema(name = "ApplicationBulkApproveRequest", description = "일괄 승인 요청")
    public static class BulkApproveRequest {
        /** 결재 처리할 신청서 목록 (각 항목에 결재자 정보 포함) */
        @Schema(description = "승인할 신청서 목록")
        private List<ApprovalItem> approvals;
    }

    /**
     * 일괄 결재 요청의 개별 항목 DTO
     *
     * <p>일괄 결재 요청({@link BulkApproveRequest})의 각 신청서별 결재 정보입니다.</p>
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @Schema(name = "ApplicationApprovalItem", description = "개별 신청서 승인 정보")
    public static class ApprovalItem {
        /** 결재할 신청관리번호 */
        @Schema(description = "신청관리번호")
        private String apfMngNo;

        /** 결재자 사원번호 */
        @Schema(description = "결재자 사원번호")
        private String dcdEno;

        /** 결재의견 */
        @Schema(description = "결재의견")
        private String dcdOpnn;

        /** 결재상태 (허용값: "승인" 또는 "반려") */
        @Schema(description = "결재상태 (승인/반려)")
        private String dcdSts;
    }

    /**
     * 일괄 결재 응답 DTO
     *
     * <p>일괄 결재 처리 후 전체/성공/실패 건수와 개별 결과 목록을 반환합니다.</p>
     */
    @Getter
    @Builder
    @Schema(name = "ApplicationBulkApproveResponse", description = "일괄 승인 응답")
    public static class BulkApproveResponse {
        /** 전체 처리 건수 (요청된 신청서 수) */
        @Schema(description = "전체 처리 건수")
        private int totalCount;

        /** 성공 건수 */
        @Schema(description = "성공 건수")
        private int successCount;

        /** 실패 건수 (전체 롤백 방식이므로 실제로는 0 또는 전체) */
        @Schema(description = "실패 건수")
        private int failureCount;

        /** 개별 승인 결과 목록 */
        @Schema(description = "개별 승인 결과 목록")
        private List<ApprovalResult> results;
    }

    /**
     * 일괄 결재 응답의 개별 결과 DTO
     *
     * <p>각 신청서별 결재 처리 결과입니다.</p>
     */
    @Getter
    @Builder
    @Schema(name = "ApplicationApprovalResult", description = "개별 승인 결과")
    public static class ApprovalResult {
        /** 처리된 신청관리번호 */
        @Schema(description = "신청관리번호")
        private String apfMngNo;

        /** 처리 성공 여부 */
        @Schema(description = "성공 여부")
        private boolean success;

        /** 처리 결과 메시지 (예: "처리 완료") */
        @Schema(description = "결과 메시지")
        private String message;
    }

    /**
     * 일괄 조회 요청 DTO
     *
     * <p>여러 신청관리번호를 한 번에 조회할 때 사용합니다.</p>
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @Schema(name = "ApplicationBulkGetRequest", description = "일괄 조회 요청")
    public static class BulkGetRequest {
        /** 조회할 신청관리번호 목록 (예: ["APF_202600000001", "APF_202600000002"]) */
        @Schema(description = "조회할 신청관리번호 목록")
        private List<String> apfMngNos;
    }

    /**
     * 신청서 조회 응답 DTO
     *
     * <p>신청서 마스터({@link Capplm}) 정보와 결재자 목록({@link Cdecim})을
     * 하나의 응답으로 반환합니다.</p>
     */
    @Getter
    @Builder
    @Schema(name = "ApplicationResponse")
    public static class Response {
        /** 신청관리번호 (PK, 예: "APF_202600000001") */
        @Schema(description = "신청관리번호")
        private String apfMngNo;

        /** 신청서명 */
        @Schema(description = "신청서명")
        private String apfNm;

        /** 신청서 세부 내용 (JSON 형식, 결재선 정보 포함) */
        @Schema(description = "신청서세부내용")
        private String apfDtlCone;

        /** 신청상태 (예: "결재중", "결재완료", "반려") */
        @Schema(description = "신청상태")
        private String apfSts;

        /** 신청자 사원번호 */
        @Schema(description = "신청자 사원번호")
        private String rqsEno;

        /** 신청일자 */
        @Schema(description = "신청일자")
        private LocalDate rqsDt;

        /** 신청의견 */
        @Schema(description = "신청의견")
        private String rqsOpnn;

        /** 결재자 목록 (순번 순서대로) */
        @Schema(description = "결재자 목록")
        private List<ApproverResponse> approvers;

        /**
         * 엔티티를 응답 DTO로 변환하는 정적 팩토리 메서드
         *
         * @param capplm    신청서 마스터 엔티티
         * @param approvers 결재자 목록 엔티티
         * @return 변환된 응답 DTO
         */
        public static Response fromEntity(Capplm capplm, List<Cdecim> approvers) {
            return Response.builder()
                    .apfMngNo(capplm.getApfMngNo())       // 신청관리번호
                    .apfNm(capplm.getApfNm())             // 신청서명
                    .apfDtlCone(capplm.getApfDtlCone())   // 신청서세부내용
                    .apfSts(capplm.getApfSts())           // 신청상태
                    .rqsEno(capplm.getRqsEno())           // 신청자 사원번호
                    .rqsDt(capplm.getRqsDt())             // 신청일자
                    .rqsOpnn(capplm.getRqsOpnn())         // 신청의견
                    .approvers(approvers.stream()
                            .map(ApproverResponse::fromEntity) // 각 결재자 엔티티를 DTO로 변환
                            .collect(Collectors.toList()))
                    .build();
        }
    }

    /**
     * 결재자 정보 응답 DTO
     *
     * <p>결재자({@link Cdecim}) 한 명의 결재 정보를 담습니다.</p>
     */
    @Getter
    @Builder
    @Schema(name = "ApplicationApproverResponse")
    public static class ApproverResponse {
        /** 결재순번 (1부터 시작, 순차 결재 순서) */
        @Schema(description = "결재순번")
        private Integer dcdSqn;

        /** 결재자 사원번호 */
        @Schema(description = "결재자 사원번호")
        private String dcdEno;

        /** 결재유형 (예: "결재", null이면 미결재) */
        @Schema(description = "결재유형")
        private String dcdTp;

        /** 결재일자 (결재 처리된 날짜) */
        @Schema(description = "결재일자")
        private LocalDate dcdDt;

        /** 결재의견 */
        @Schema(description = "결재의견")
        private String dcdOpnn;

        /** 결재상태 (예: "승인", "반려", null이면 미결재) */
        @Schema(description = "결재상태")
        private String dcdSts;

        /**
         * 결재 엔티티를 응답 DTO로 변환하는 정적 팩토리 메서드
         *
         * @param cdecim 결재 엔티티
         * @return 변환된 결재자 응답 DTO
         */
        public static ApproverResponse fromEntity(Cdecim cdecim) {
            return ApproverResponse.builder()
                    .dcdSqn(cdecim.getDcdSqn())   // 결재순번
                    .dcdEno(cdecim.getDcdEno())   // 결재자 사원번호
                    .dcdTp(cdecim.getDcdTp())     // 결재유형
                    .dcdDt(cdecim.getDcdDt())     // 결재일자
                    .dcdOpnn(cdecim.getDcdOpnn()) // 결재의견
                    .dcdSts(cdecim.getDcdSts())   // 결재상태
                    .build();
        }
    }
}
