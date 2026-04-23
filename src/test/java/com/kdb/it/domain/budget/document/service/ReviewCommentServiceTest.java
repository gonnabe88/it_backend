package com.kdb.it.domain.budget.document.service;

import com.kdb.it.domain.budget.document.dto.ReviewCommentDto;
import com.kdb.it.domain.budget.document.entity.Brivgm;
import com.kdb.it.domain.budget.document.repository.BrivgmRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

/**
 * 검토의견 서비스({@link ReviewCommentService}) 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
class ReviewCommentServiceTest {

    @Mock BrivgmRepository brivgmRepository;
    @InjectMocks ReviewCommentService reviewCommentService;

    @Test
    void 코멘트_추가시_리포지토리_save가_호출된다() {
        // 준비: save 호출 시 반환될 엔티티 구성
        var entity = Brivgm.create("DOC-2026-0010", new BigDecimal("1.01"),
                "G", "테스트 코멘트", null, null);
        given(brivgmRepository.save(any(Brivgm.class))).willReturn(entity);

        // 실행
        reviewCommentService.addComment("DOC-2026-0010",
                createRequest(new BigDecimal("1.01"), "G", "테스트 코멘트", null, null));

        // 검증: save 호출 여부 확인
        then(brivgmRepository).should().save(any(Brivgm.class));
    }

    @Test
    void 코멘트_조회시_해당_버전의_미삭제_코멘트만_반환된다() {
        // 준비
        var comment = Brivgm.create("DOC-2026-0010", new BigDecimal("1.01"),
                "G", "전반 코멘트", null, null);
        given(brivgmRepository.findByDocMngNoAndDocVrsAndDelYnOrderByFstEnrDtmAsc(
                "DOC-2026-0010", new BigDecimal("1.01"), "N"))
                .willReturn(List.of(comment));

        // 실행
        List<ReviewCommentDto.Response> result =
                reviewCommentService.getComments("DOC-2026-0010", new BigDecimal("1.01"));

        // 검증
        assertThat(result).hasSize(1);
    }

    @Test
    void 존재하지_않는_코멘트_해결처리시_예외가_발생한다() {
        // 준비: 빈 Optional 반환
        given(brivgmRepository.findByIvgSnoAndDelYn("UNKNOWN", "N"))
                .willReturn(Optional.empty());

        // 실행 & 검증
        assertThatThrownBy(() -> reviewCommentService.resolveComment("UNKNOWN"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 코멘트_해결처리시_rslvYn이_Y로_변경된다() {
        // 준비
        var comment = Brivgm.create("DOC-2026-0010", new BigDecimal("1.01"),
                "G", "코멘트", null, null);
        given(brivgmRepository.findByIvgSnoAndDelYn(anyString(), eq("N")))
                .willReturn(Optional.of(comment));

        // 실행
        reviewCommentService.resolveComment("some-ivg-sno");

        // 검증
        assertThat(comment.getRslvYn()).isEqualTo("Y");
    }

    // 헬퍼: CreateRequest 인스턴스를 reflection으로 생성
    private ReviewCommentDto.CreateRequest createRequest(
            BigDecimal docVrs, String ivgTp, String ivgCone,
            String markId, String qtdCone) {
        try {
            var req = new ReviewCommentDto.CreateRequest();
            setField(req, "docVrs", docVrs);
            setField(req, "ivgTp", ivgTp);
            setField(req, "ivgCone", ivgCone);
            setField(req, "markId", markId);
            setField(req, "qtdCone", qtdCone);
            return req;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void setField(Object obj, String fieldName, Object value) throws Exception {
        var field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(obj, value);
    }
}
