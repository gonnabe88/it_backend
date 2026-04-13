package com.kdb.it.domain.budget.document.service;

import com.kdb.it.domain.budget.document.entity.Brdocm;
import com.kdb.it.domain.budget.document.dto.ServiceRequestDocDto;
import com.kdb.it.domain.budget.document.repository.ServiceRequestDocRepository;
import com.kdb.it.common.iam.repository.UserRepository;
import com.kdb.it.common.util.HtmlSanitizer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

/**
 * 요구사항 정의서(TAAABB_BRDOCM) 서비스
 *
 * <p>
 * 요구사항 정의서 엔티티의 CRUD 비즈니스 로직을 처리합니다.
 * </p>
 *
 * <p>
 * Soft Delete 패턴: {@code DEL_YN='Y'}로 논리 삭제합니다.
 * </p>
 *
 * <p>
 * {@code @Transactional(readOnly = true)}: 조회 메서드의 기본값.
 * 쓰기 메서드는 {@code @Transactional}로 오버라이드합니다.
 * </p>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ServiceRequestDocService {

    /** 요구사항 정의서 데이터 접근 리포지토리 (TAAABB_BRDOCM) */
    private final ServiceRequestDocRepository serviceRequestDocRepository;

    /** 사용자 정보 리포지토리 (TAAABB_CUSERI): 사번→사용자명 조회용 */
    private final UserRepository cuserIRepository;

    /**
     * 요구사항 정의서 목록 조회
     *
     * <p>
     * 삭제되지 않은({@code DEL_YN='N'}) 모든 요구사항 정의서를 조회합니다.
     * </p>
     *
     * @return 요구사항 정의서 응답 DTO 목록
     */
    public List<ServiceRequestDocDto.Response> getDocumentList() {
        return serviceRequestDocRepository.findAllByDelYn("N").stream()
                .map(entity -> {
                    ServiceRequestDocDto.Response response = ServiceRequestDocDto.Response.fromEntity(entity);
                    // 최초생성자 사번 → 사용자명 매핑
                    if (response.getFstEnrUsid() != null && !response.getFstEnrUsid().isEmpty()) {
                        cuserIRepository.findById(response.getFstEnrUsid())
                                .ifPresent(user -> response.setFstEnrUsNm(user.getUsrNm()));
                    }
                    return response;
                })
                .toList();
    }

    /**
     * 요구사항 정의서 단건 조회
     *
     * <p>
     * 문서관리번호로 삭제되지 않은 요구사항 정의서를 조회합니다.
     * </p>
     *
     * @param docMngNo 문서관리번호 (예: DOC-2026-0001)
     * @return 요구사항 정의서 응답 DTO
     * @throws IllegalArgumentException 해당 문서관리번호가 없는 경우
     */
    public ServiceRequestDocDto.Response getDocument(String docMngNo) {
        Brdocm document = serviceRequestDocRepository.findByDocMngNoAndDelYn(docMngNo, "N")
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 문서관리번호입니다: " + docMngNo));
        return ServiceRequestDocDto.Response.fromEntity(document);
    }

    /**
     * 요구사항 정의서 생성
     *
     * <p>
     * 문서관리번호({@code DOC_MNG_NO})가 없으면 Oracle 시퀀스로 자동 채번합니다.
     * </p>
     *
     * <p>
     * 자동 채번 형식: {@code DOC-{연도}-{seq:04d}} (예: DOC-2026-0001)
     * </p>
     *
     * <p>
     * 요구사항내용({@code reqCone})은 XSS 방지를 위해 HTML 새니타이징을 적용합니다.
     * </p>
     *
     * @param request 요구사항 정의서 생성 요청 DTO
     * @return 생성된 문서관리번호
     * @throws IllegalArgumentException 제공된 문서관리번호가 이미 존재하는 경우
     */
    @Transactional
    public String createDocument(ServiceRequestDocDto.CreateRequest request) {
        String docMngNo = request.getDocMngNo();

        // 문서관리번호가 없으면 자동 채번
        if (docMngNo == null || docMngNo.isEmpty()) {
            Long nextVal = serviceRequestDocRepository.getNextSequenceValue();
            String year = String.valueOf(LocalDate.now().getYear());
            docMngNo = String.format("DOC-%s-%04d", year, nextVal);
            request.setDocMngNo(docMngNo);
        } else {
            // 제공된 문서관리번호 중복 확인
            if (serviceRequestDocRepository.existsByDocMngNoAndDelYn(docMngNo, "N")) {
                throw new IllegalArgumentException("이미 존재하는 문서관리번호입니다: " + docMngNo);
            }
        }

        // 요구사항내용 XSS 새니타이징
        request.setReqCone(HtmlSanitizer.sanitize(request.getReqCone()));

        Brdocm document = request.toEntity();
        serviceRequestDocRepository.save(document);
        return document.getDocMngNo();
    }

    /**
     * 요구사항 정의서 수정
     *
     * <p>
     * 문서관리번호로 요구사항 정의서를 조회하여 정보를 수정합니다.
     * 요구사항내용({@code reqCone})은 XSS 방지를 위해 HTML 새니타이징을 적용합니다.
     * </p>
     *
     * @param docMngNo 수정할 문서관리번호
     * @param request  수정 요청 DTO
     * @return 수정된 문서관리번호
     * @throws IllegalArgumentException 해당 문서관리번호가 없는 경우
     */
    @Transactional
    public String updateDocument(String docMngNo, ServiceRequestDocDto.UpdateRequest request) {
        Brdocm document = serviceRequestDocRepository.findByDocMngNoAndDelYn(docMngNo, "N")
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 문서관리번호입니다: " + docMngNo));

        // 요구사항내용 XSS 새니타이징
        String sanitizedCone = HtmlSanitizer.sanitize(request.getReqCone());
        byte[] reqConeBytes = sanitizedCone != null ? sanitizedCone.getBytes(StandardCharsets.UTF_8) : null;

        // JPA Dirty Checking으로 자동 반영
        document.update(
                request.getReqNm(),
                reqConeBytes,
                request.getReqDtt(),
                request.getBzDtt(),
                request.getFsgTlm());

        return docMngNo;
    }

    /**
     * 요구사항 정의서 삭제 (Soft Delete)
     *
     * <p>
     * {@code DEL_YN='Y'}로 논리 삭제합니다. 물리 삭제는 수행하지 않습니다.
     * </p>
     *
     * @param docMngNo 삭제할 문서관리번호
     * @throws IllegalArgumentException 해당 문서관리번호가 없는 경우
     */
    @Transactional
    public void deleteDocument(String docMngNo) {
        Brdocm document = serviceRequestDocRepository.findByDocMngNoAndDelYn(docMngNo, "N")
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 문서관리번호입니다: " + docMngNo));

        // Soft Delete (DEL_YN='Y')
        document.delete();
    }
}
