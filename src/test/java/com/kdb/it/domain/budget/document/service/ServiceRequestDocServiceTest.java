package com.kdb.it.domain.budget.document.service;

import com.kdb.it.common.iam.repository.UserRepository;
import com.kdb.it.domain.budget.document.dto.ServiceRequestDocDto;
import com.kdb.it.domain.budget.document.entity.Brdocm;
import com.kdb.it.domain.budget.document.repository.ServiceRequestDocRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

/**
 * ServiceRequestDocService 단위 테스트
 *
 * <p>
 * 복합키 (DOC_MNG_NO, DOC_VRS) 기반 요구사항 정의서 서비스의
 * 버전 관리 로직을 검증합니다.
 * </p>
 *
 * <p>
 * TDD Red 단계: 아직 구현되지 않은 메서드(createNewVersion, 버전 지정 조회/삭제)에
 * 대한 기대 동작을 먼저 정의합니다.
 * </p>
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ServiceRequestDocServiceTest {

    /** 요구사항 정의서 리포지토리 (mock) */
    @Mock
    private ServiceRequestDocRepository repository;

    /** 사용자 정보 리포지토리 (mock): 서비스 의존성 충족용 */
    @Mock
    private UserRepository cuserIRepository;

    /** 테스트 대상 서비스 */
    @InjectMocks
    private ServiceRequestDocService service;

    @Test
    @DisplayName("신규 문서 생성 시 버전은 0.01 이다")
    void createDocument_setsInitialVersion() {
        // Arrange
        given(repository.getNextSequenceValue()).willReturn(1L);
        given(repository.existsByDocMngNoAndDelYn(anyString(), eq("N"))).willReturn(false);
        ServiceRequestDocDto.CreateRequest req = ServiceRequestDocDto.CreateRequest.builder()
                .reqNm("테스트 문서")
                .build();
        Brdocm saved = Brdocm.builder()
                .docMngNo("DOC-001")
                .docVrs(new BigDecimal("0.01"))
                .reqNm("테스트 문서")
                .build();
        given(repository.save(any(Brdocm.class))).willReturn(saved);

        // Act
        String result = service.createDocument(req);

        // Assert
        then(repository).should().save(argThat(entity ->
                new BigDecimal("0.01").compareTo(entity.getDocVrs()) == 0
        ));
        assertThat(result).isNotBlank();
    }

    @Test
    @DisplayName("새 버전 생성 시 기존 최신 버전 + 0.01 로 생성된다")
    void createNewVersion_incrementsVersion() {
        // Arrange
        Brdocm latest = Brdocm.builder()
                .docMngNo("DOC-001")
                .docVrs(new BigDecimal("0.01"))
                .reqNm("문서")
                .build();
        given(repository.findTopByDocMngNoAndDelYnOrderByDocVrsDesc("DOC-001", "N"))
                .willReturn(Optional.of(latest));
        given(repository.save(any(Brdocm.class))).willAnswer(inv -> inv.getArgument(0));

        // Act
        BigDecimal newVersion = service.createNewVersion("DOC-001");

        // Assert
        assertThat(newVersion).isEqualByComparingTo(new BigDecimal("0.02"));
        then(repository).should().save(argThat(entity ->
                new BigDecimal("0.02").compareTo(entity.getDocVrs()) == 0
        ));
    }

    @Test
    @DisplayName("새 버전 생성 시 문서가 없으면 예외가 발생한다")
    void createNewVersion_throwsWhenNotFound() {
        given(repository.findTopByDocMngNoAndDelYnOrderByDocVrsDesc("MISSING", "N"))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> service.createNewVersion("MISSING"))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("version 파라미터 없이 조회 시 최신 버전을 반환한다")
    void getDocument_withoutVersion_returnsLatest() {
        Brdocm latest = Brdocm.builder()
                .docMngNo("DOC-001")
                .docVrs(new BigDecimal("0.03"))
                .reqNm("최신 문서")
                .build();
        given(repository.findTopByDocMngNoAndDelYnOrderByDocVrsDesc("DOC-001", "N"))
                .willReturn(Optional.of(latest));

        ServiceRequestDocDto.Response result = service.getDocument("DOC-001", null);

        assertThat(result.getDocVrs()).isEqualByComparingTo(new BigDecimal("0.03"));
    }

    @Test
    @DisplayName("version 파라미터 지정 시 해당 버전을 반환한다")
    void getDocument_withVersion_returnsSpecific() {
        Brdocm v1 = Brdocm.builder()
                .docMngNo("DOC-001")
                .docVrs(new BigDecimal("0.01"))
                .reqNm("v0.01 문서")
                .build();
        given(repository.findByDocMngNoAndDocVrsAndDelYn("DOC-001", new BigDecimal("0.01"), "N"))
                .willReturn(Optional.of(v1));

        ServiceRequestDocDto.Response result = service.getDocument("DOC-001", new BigDecimal("0.01"));

        assertThat(result.getDocVrs()).isEqualByComparingTo(new BigDecimal("0.01"));
    }

    @Test
    @DisplayName("version 없이 삭제 시 전체 버전이 소프트 삭제된다")
    void deleteDocument_withoutVersion_deletesAll() {
        Brdocm v1 = Brdocm.builder().docMngNo("DOC-001").docVrs(new BigDecimal("0.01")).build();
        Brdocm v2 = Brdocm.builder().docMngNo("DOC-001").docVrs(new BigDecimal("0.02")).build();
        given(repository.findAllByDocMngNoAndDelYn("DOC-001", "N")).willReturn(List.of(v1, v2));

        service.deleteDocument("DOC-001", null);

        // BaseEntity.delete()가 호출되어 delYn이 'Y'로 변경됨 (JPA Dirty Checking)
        assertThat(v1.getDelYn()).isEqualTo("Y");
        assertThat(v2.getDelYn()).isEqualTo("Y");
    }
}
