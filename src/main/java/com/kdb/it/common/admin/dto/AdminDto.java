package com.kdb.it.common.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 관리자 기능 DTO 모음
 *
 * <p>Static Nested Class 방식으로 요청/응답 DTO를 하나의 파일에 관리합니다.</p>
 */
public class AdminDto {

    // =========================================================================
    // 공통코드 (TAAABB_CCODEM)
    // =========================================================================

    /**
     * 공통코드 생성/수정 요청 DTO
     */
    @Schema(name = "AdminDto.CodeRequest", description = "공통코드 생성/수정 요청")
    public record CodeRequest(
            @NotBlank @Schema(description = "코드ID") String cdId,
            @Schema(description = "코드명") String cdNm,
            @Schema(description = "코드값") String cdva,
            @Schema(description = "코드설명") String cdDes,
            @Schema(description = "코드값구분") String cttTp,
            @Schema(description = "코드값구분설명") String cttTpDes,
            @Schema(description = "시작일자") LocalDate sttDt,
            @Schema(description = "종료일자") LocalDate endDt,
            @Schema(description = "코드순서") Integer cdSqn
    ) {}

    /**
     * 공통코드 조회 응답 DTO
     * 최초생성자·마지막수정자 사원번호를 이름으로 변환하여 제공합니다.
     */
    @Schema(name = "AdminDto.CodeResponse", description = "공통코드 조회 응답")
    public record CodeResponse(
            String cdId,
            String cdNm,
            String cdva,
            String cdDes,
            String cttTp,
            String cttTpDes,
            LocalDate sttDt,
            LocalDate endDt,
            Integer cdSqn,
            LocalDateTime fstEnrDtm,
            String fstEnrUsid,
            String fstEnrUsNm,   // 최초생성자 이름 (ENO → 이름 변환)
            LocalDateTime lstChgDtm,
            String lstChgUsid,
            String lstChgUsNm    // 마지막수정자 이름 (ENO → 이름 변환)
    ) {}

    // =========================================================================
    // 자격등급 (TAAABB_CAUTHI) — Session 2 구현 예정
    // =========================================================================

    @Schema(name = "AdminDto.AuthGradeRequest", description = "자격등급 생성/수정 요청")
    public record AuthGradeRequest(
            @NotBlank String athId,
            String qlfGrNm,
            String qlfGrMat,
            String useYn
    ) {}

    @Schema(name = "AdminDto.AuthGradeResponse", description = "자격등급 조회 응답")
    public record AuthGradeResponse(
            String athId,
            String qlfGrNm,
            String qlfGrMat,
            String useYn,
            LocalDateTime fstEnrDtm,
            String fstEnrUsid,
            String fstEnrUsNm,
            LocalDateTime lstChgDtm,
            String lstChgUsid,
            String lstChgUsNm
    ) {}

    // =========================================================================
    // 사용자 (TAAABB_CUSERI) — Session 2 구현 예정
    // =========================================================================

    @Schema(name = "AdminDto.UserRequest", description = "사용자 생성/수정 요청")
    public record UserRequest(
            @NotBlank String eno,
            String usrNm,
            String ptCNm,
            String temC,
            String bbrC,
            String etrMilAddrNm,
            String inleNo,
            String cpnTpn,
            String password
    ) {}

    @Schema(name = "AdminDto.UserResponse", description = "사용자 조회 응답")
    public record UserResponse(
            String eno,
            String usrNm,
            String ptCNm,
            String temC,
            String temNm,
            String bbrC,
            String bbrNm,
            String etrMilAddrNm,
            String inleNo,
            String cpnTpn,
            LocalDateTime fstEnrDtm,
            LocalDateTime lstChgDtm
    ) {}

    // =========================================================================
    // 조직 (TAAABB_CORGNI) — Session 3 구현 예정
    // =========================================================================

    @Schema(name = "AdminDto.OrgRequest", description = "조직 생성/수정 요청")
    public record OrgRequest(
            @NotBlank String prlmOgzCCone,
            String bbrNm,
            String bbrWrenNm,
            String itmSqnSno,
            String prlmHrkOgzCCone
    ) {}

    @Schema(name = "AdminDto.OrgResponse", description = "조직 조회 응답")
    public record OrgResponse(
            String prlmOgzCCone,
            String bbrNm,
            String bbrWrenNm,
            String itmSqnSno,
            String prlmHrkOgzCCone,
            LocalDateTime fstEnrDtm,
            String fstEnrUsid,
            String fstEnrUsNm,
            LocalDateTime lstChgDtm,
            String lstChgUsid,
            String lstChgUsNm
    ) {}

    // =========================================================================
    // 역할 (TAAABB_CROLEI) — Session 2 구현 예정
    // =========================================================================

    @Schema(name = "AdminDto.RoleRequest", description = "역할 생성/수정 요청")
    public record RoleRequest(
            @NotBlank String athId,
            @NotBlank String eno,
            String useYn
    ) {}

    @Schema(name = "AdminDto.RoleResponse", description = "역할 조회 응답")
    public record RoleResponse(
            String athId,
            String eno,
            String usrNm,    // ENO → 이름 변환
            String useYn,
            LocalDateTime fstEnrDtm,
            String fstEnrUsid,
            String fstEnrUsNm,
            LocalDateTime lstChgDtm,
            String lstChgUsid,
            String lstChgUsNm
    ) {}

    // =========================================================================
    // 로그인 이력 (TAAABB_CLOGNH) — Session 3 구현 예정
    // =========================================================================

    @Schema(name = "AdminDto.LoginHistoryResponse", description = "로그인 이력 조회 응답")
    public record LoginHistoryResponse(
            String eno,
            String usrNm,    // ENO → 이름 변환
            LocalDateTime lgnDtm,
            String lgnTp,
            String ipAddr,
            String flurRsn,
            String ustAgt,
            LocalDateTime fstEnrDtm
    ) {}

    // =========================================================================
    // JWT 토큰 (TAAABB_CRTOKM) — Session 3 구현 예정
    // =========================================================================

    @Schema(name = "AdminDto.TokenResponse", description = "JWT 토큰 조회 응답")
    public record TokenResponse(
            String eno,
            String usrNm,    // ENO → 이름 변환
            LocalDateTime endDtm,
            String tokMasked,    // 앞 20자 + "..." 마스킹
            LocalDateTime fstEnrDtm
    ) {}

    // =========================================================================
    // 첨부파일 (TAAABB_CFILEM) — Session 3 구현 예정
    // =========================================================================

    @Schema(name = "AdminDto.FileResponse", description = "첨부파일 조회 응답")
    public record FileResponse(
            String flMngNo,
            String orcFlNm,
            String flDtt,
            String orcDtt,
            LocalDateTime fstEnrDtm,
            String fstEnrUsid,
            String fstEnrUsNm
    ) {}

    // =========================================================================
    // 대시보드 통계 — Session 3 구현 예정
    // =========================================================================

    @Schema(name = "AdminDto.LoginStatResponse", description = "일별 로그인 통계")
    public record LoginStatResponse(
            LocalDate date,
            Long count
    ) {}
}
