package com.kdb.it.domain.budget.document.service;

import com.kdb.it.domain.budget.document.dto.ReviewCommentDto;
import com.kdb.it.domain.budget.document.repository.BrivgmRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 문서 검토의견(Brivgm) 서비스
 *
 * <p>
 * 특정 문서+버전의 검토의견 조회, 생성, 해결처리 기능을 제공합니다.
 * </p>
 */
@Service
@RequiredArgsConstructor
public class ReviewCommentService {

    private final BrivgmRepository brivgmRepository;

    /**
     * 특정 문서+버전의 미삭제 검토의견 목록을 조회합니다.
     *
     * @param docMngNo 문서관리번호
     * @param docVrs   문서버전
     * @return 검토의견 응답 DTO 목록 (생성일시 오름차순)
     */
    @Transactional(readOnly = true)
    public List<ReviewCommentDto.Response> getComments(String docMngNo, BigDecimal docVrs) {
        return brivgmRepository
                .findByDocMngNoAndDocVrsAndDelYnOrderByFstEnrDtmAsc(docMngNo, docVrs, "N")
                .stream()
                .map(e -> new ReviewCommentDto.Response(e, resolveAuthorName(e.getFstEnrUsid())))
                .collect(Collectors.toList());
    }

    /**
     * 검토의견을 신규 등록합니다.
     *
     * @param docMngNo 대상 문서관리번호
     * @param request  검토의견 생성 요청 DTO
     * @return 저장된 검토의견 응답 DTO
     */
    @Transactional
    public ReviewCommentDto.Response addComment(String docMngNo,
                                                 ReviewCommentDto.CreateRequest request) {
        var saved = brivgmRepository.save(request.toEntity(docMngNo));
        return new ReviewCommentDto.Response(saved, resolveAuthorName(saved.getFstEnrUsid()));
    }

    /**
     * 검토의견을 해결 처리합니다. RSLV_YN을 'Y'로 변경합니다.
     *
     * <p>
     * URL path의 {@code docMngNo}와 실제 코멘트가 소속된 문서가 일치하는지 함께 검증하여
     * 다른 문서의 {@code ivgSno}를 이용한 교차 접근을 차단합니다.
     * </p>
     *
     * @param docMngNo 문서관리번호 (소속 검증용)
     * @param ivgSno   의견일련번호
     * @throws ResponseStatusException 해당 의견이 존재하지 않거나 다른 문서에 속한 경우 (404 NOT_FOUND)
     */
    @Transactional
    public void resolveComment(String docMngNo, String ivgSno) {
        var comment = brivgmRepository.findByIvgSnoAndDocMngNoAndDelYn(ivgSno, docMngNo, "N")
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "검토의견을 찾을 수 없습니다: " + ivgSno));
        comment.resolve();
    }

    /**
     * 사번으로 사용자 이름을 조회합니다.
     *
     * <p>
     * TODO: 기존 ServiceRequestDocService의 userRepository 패턴과 동일하게 교체
     * </p>
     *
     * @param eno 사번
     * @return 사용자 이름 (현재는 사번 자체를 반환)
     */
    private String resolveAuthorName(String eno) {
        if (eno == null) return "";
        return eno;
    }
}
