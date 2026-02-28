package com.kdb.it.dto;

import com.kdb.it.domain.entity.CuserI;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 사용자(직원) 관련 DTO 클래스 모음
 *
 * <p>사용자 정보(TAAABB_CUSERI) 조회에 사용되는 Response DTO를
 * 정적 중첩 클래스(Static Nested Class) 형태로 관리합니다.</p>
 *
 * <p>포함된 DTO:</p>
 * <ul>
 *   <li>{@link ListResponse}: 부점별 사용자 목록 조회 응답 (기본 정보)</li>
 *   <li>{@link DetailResponse}: 사번별 사용자 상세 조회 응답 (연락처 등 추가 정보)</li>
 * </ul>
 *
 * <p>부점명({@code bbrNm})은 {@link CuserI} 엔티티의 {@code @ManyToOne} 연관관계
 * ({@link com.kdb.it.domain.entity.CorgnI})에서 가져오므로,
 * {@code fromEntity()} 메서드에 별도 파라미터로 전달합니다.</p>
 */
public class UserDto {

    /**
     * 사용자 목록 조회 응답 DTO
     *
     * <p>부점코드별 사용자 목록 조회 시 반환되는 기본 정보입니다.
     * 상세 연락처 정보는 포함하지 않습니다.</p>
     *
     * <p>{@link #fromEntity(CuserI, String)} 정적 팩토리 메서드로 엔티티에서 변환합니다.</p>
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(name = "UserListResponse")
    public static class ListResponse {
        /** 행번(사번) - 직원 고유 식별자 */
        @Schema(description = "행번")
        private String eno;

        /**
         * 부점명
         * <p>{@link com.kdb.it.domain.entity.CorgnI#getBbrNm()}에서 조회됩니다.
         * {@code CuserI.bbrC} 필드로 연결된 {@code CorgnI}의 부점명입니다.</p>
         */
        @Schema(description = "부점명")
        private String bbrNm;

        /** 팀명 (TEM_NM) */
        @Schema(description = "팀명")
        private String temNm;

        /** 사용자명 (USR_NM, 한국어 이름) */
        @Schema(description = "사용자명")
        private String usrNm;

        /** 직위명 (PT_C_NM, 예: "과장", "팀장", "부장") */
        @Schema(description = "직위명")
        private String ptCNm;

        /**
         * {@link CuserI} 엔티티를 목록 응답 DTO로 변환하는 정적 팩토리 메서드
         *
         * <p>부점명({@code bbrNm})은 {@link CuserI}가 직접 갖지 않고 연관관계에서 조회하므로,
         * {@link com.kdb.it.domain.entity.CuserI#getBbrNm()} 결과를 파라미터로 전달합니다.</p>
         *
         * @param user   변환할 CuserI 엔티티
         * @param bbrNm  부점명 (CorgnI.bbrNm, CuserI.getBbrNm()으로 획득)
         * @return 변환된 목록 응답 DTO
         */
        public static ListResponse fromEntity(CuserI user, String bbrNm) {
            return ListResponse.builder()
                    .eno(user.getEno())       // 행번(사번)
                    .bbrNm(bbrNm)            // 부점명 (연관관계에서 조회)
                    .temNm(user.getTemNm())   // 팀명
                    .usrNm(user.getUsrNm())   // 사용자명
                    .ptCNm(user.getPtCNm())   // 직위명
                    .build();
        }
    }

    /**
     * 사용자 상세 조회 응답 DTO
     *
     * <p>사번별 상세 조회 시 반환됩니다. {@link ListResponse}의 기본 정보 외에
     * 연락처(내선번호, 휴대폰번호)와 상세직무 정보를 추가로 포함합니다.</p>
     *
     * <p>{@link #fromEntity(CuserI, String)} 정적 팩토리 메서드로 엔티티에서 변환합니다.</p>
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(name = "UserDetailResponse")
    public static class DetailResponse {
        /** 행번(사번) */
        @Schema(description = "행번")
        private String eno;

        /** 부점명 (연관관계의 CorgnI.bbrNm) */
        @Schema(description = "부점명")
        private String bbrNm;

        /** 팀명 */
        @Schema(description = "팀명")
        private String temNm;

        /** 사용자명 */
        @Schema(description = "사용자명")
        private String usrNm;

        /** 직위명 */
        @Schema(description = "직위명")
        private String ptCNm;

        /** 내선번호 (INLE_NO, 사내 전화 번호) */
        @Schema(description = "내선번호")
        private String inleNo;

        /** 휴대폰번호 (CPN_TPN) */
        @Schema(description = "휴대폰번호")
        private String cpnTpn;

        /** 상세직무내용 (DTS_DTL_CONE, 담당 업무 설명) */
        @Schema(description = "상세직무내용")
        private String dtsDtlCone;

        /**
         * {@link CuserI} 엔티티를 상세 응답 DTO로 변환하는 정적 팩토리 메서드
         *
         * @param user   변환할 CuserI 엔티티
         * @param bbrNm  부점명 (CorgnI.bbrNm, CuserI.getBbrNm()으로 획득)
         * @return 변환된 상세 응답 DTO
         */
        public static DetailResponse fromEntity(CuserI user, String bbrNm) {
            return DetailResponse.builder()
                    .eno(user.getEno())               // 행번(사번)
                    .bbrNm(bbrNm)                    // 부점명 (연관관계에서 조회)
                    .temNm(user.getTemNm())           // 팀명
                    .usrNm(user.getUsrNm())           // 사용자명
                    .ptCNm(user.getPtCNm())           // 직위명
                    .inleNo(user.getInleNo())         // 내선번호
                    .cpnTpn(user.getCpnTpn())         // 휴대폰번호
                    .dtsDtlCone(user.getDtsDtlCone()) // 상세직무내용
                    .build();
        }
    }
}
