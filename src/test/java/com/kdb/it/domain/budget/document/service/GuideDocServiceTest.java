package com.kdb.it.domain.budget.document.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.kdb.it.domain.budget.document.dto.GuideDocDto;
import com.kdb.it.domain.budget.document.entity.Bgdocm;
import com.kdb.it.domain.budget.document.repository.GuideDocRepository;

/**
 * GuideDocService 단위 테스트
 *
 * <p>
 * 가이드 문서 서비스의 CRUD 메서드를 검증합니다.
 * Bgdocm 엔티티는 protected 생성자를 우회하기 위해 Mockito.mock()으로 생성합니다.
 * Oracle DB 없이 실행됩니다.
 * </p>
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GuideDocServiceTest {

    @Mock
    private GuideDocRepository guideDocRepository;

    @InjectMocks
    private GuideDocService guideDocService;

    private Bgdocm mockDocument(String docMngNo, String docNm) {
        Bgdocm doc = mock(Bgdocm.class);
        given(doc.getDocMngNo()).willReturn(docMngNo);
        given(doc.getDocNm()).willReturn(docNm);
        given(doc.getDocCone()).willReturn(null);
        given(doc.getDelYn()).willReturn("N");
        return doc;
    }

    // ───────────────────────────────────────────────────────
    // getDocumentList
    // ───────────────────────────────────────────────────────

    @Test
    @DisplayName("getDocumentList: 삭제되지 않은 문서 목록을 DTO로 반환한다")
    void getDocumentList_문서있음_DTO목록반환() {
        // given
        Bgdocm doc1 = mockDocument("GDOC-2026-0001", "가이드문서1");
        Bgdocm doc2 = mockDocument("GDOC-2026-0002", "가이드문서2");
        given(guideDocRepository.findAllByDelYn("N")).willReturn(List.of(doc1, doc2));

        // when
        List<GuideDocDto.Response> result = guideDocService.getDocumentList();

        // then
        assertThat(result).hasSize(2);
        verify(guideDocRepository).findAllByDelYn("N");
    }

    @Test
    @DisplayName("getDocumentList: 문서가 없으면 빈 목록을 반환한다")
    void getDocumentList_문서없음_빈목록반환() {
        // given
        given(guideDocRepository.findAllByDelYn("N")).willReturn(List.of());

        // when
        List<GuideDocDto.Response> result = guideDocService.getDocumentList();

        // then
        assertThat(result).isEmpty();
    }

    // ───────────────────────────────────────────────────────
    // getDocument
    // ───────────────────────────────────────────────────────

    @Test
    @DisplayName("getDocument: 존재하는 문서관리번호로 조회하면 DTO를 반환한다")
    void getDocument_존재하는문서_DTO반환() {
        // given
        Bgdocm doc = mockDocument("GDOC-2026-0001", "가이드문서");
        given(guideDocRepository.findByDocMngNoAndDelYn("GDOC-2026-0001", "N")).willReturn(Optional.of(doc));

        // when
        GuideDocDto.Response result = guideDocService.getDocument("GDOC-2026-0001");

        // then
        assertThat(result.getDocMngNo()).isEqualTo("GDOC-2026-0001");
    }

    @Test
    @DisplayName("getDocument: 존재하지 않는 문서관리번호이면 IllegalArgumentException을 던진다")
    void getDocument_존재하지않는문서_IllegalArgumentException발생() {
        // given
        given(guideDocRepository.findByDocMngNoAndDelYn("INVALID", "N")).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> guideDocService.getDocument("INVALID"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("INVALID");
    }

    // ───────────────────────────────────────────────────────
    // createDocument
    // ───────────────────────────────────────────────────────

    @Test
    @DisplayName("createDocument: 문서관리번호 미입력 시 시퀀스로 자동 채번하여 생성한다")
    void createDocument_번호미입력_자동채번생성() {
        // given
        given(guideDocRepository.getNextSequenceValue()).willReturn(1L);
        GuideDocDto.CreateRequest request = GuideDocDto.CreateRequest.builder()
                .docNm("가이드문서")
                .docCone("<p>내용</p>")
                .build();

        // when
        String result = guideDocService.createDocument(request);

        // then
        assertThat(result).startsWith("GDOC-");
        verify(guideDocRepository).save(any(Bgdocm.class));
    }

    @Test
    @DisplayName("createDocument: 이미 존재하는 문서관리번호이면 IllegalArgumentException을 던진다")
    void createDocument_중복번호_IllegalArgumentException발생() {
        // given
        given(guideDocRepository.existsByDocMngNoAndDelYn("GDOC-2026-0001", "N")).willReturn(true);
        GuideDocDto.CreateRequest request = GuideDocDto.CreateRequest.builder()
                .docMngNo("GDOC-2026-0001")
                .docNm("가이드문서")
                .build();

        // when & then
        assertThatThrownBy(() -> guideDocService.createDocument(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("GDOC-2026-0001");
    }

    // ───────────────────────────────────────────────────────
    // updateDocument
    // ───────────────────────────────────────────────────────

    @Test
    @DisplayName("updateDocument: 존재하지 않는 문서관리번호이면 IllegalArgumentException을 던진다")
    void updateDocument_존재하지않는문서_IllegalArgumentException발생() {
        // given
        given(guideDocRepository.findByDocMngNoAndDelYn("INVALID", "N")).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> guideDocService.updateDocument("INVALID", new GuideDocDto.UpdateRequest()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("INVALID");
    }

    // ───────────────────────────────────────────────────────
    // deleteDocument
    // ───────────────────────────────────────────────────────

    @Test
    @DisplayName("deleteDocument: 존재하지 않는 문서관리번호이면 IllegalArgumentException을 던진다")
    void deleteDocument_존재하지않는문서_IllegalArgumentException발생() {
        // given
        given(guideDocRepository.findByDocMngNoAndDelYn("INVALID", "N")).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> guideDocService.deleteDocument("INVALID"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("INVALID");
    }

    @Test
    @DisplayName("deleteDocument: 존재하는 문서를 논리 삭제한다")
    void deleteDocument_존재하는문서_논리삭제수행() {
        // given
        Bgdocm doc = mockDocument("GDOC-2026-0001", "가이드문서");
        given(guideDocRepository.findByDocMngNoAndDelYn("GDOC-2026-0001", "N")).willReturn(Optional.of(doc));

        // when
        guideDocService.deleteDocument("GDOC-2026-0001");

        // then
        verify(doc).delete();
    }
}
