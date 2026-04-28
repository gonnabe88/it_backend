package com.kdb.it.common.admin.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.kdb.it.common.admin.dto.AdminDto;
import com.kdb.it.common.code.entity.Ccodem;
import com.kdb.it.common.code.repository.CodeRepository;
import com.kdb.it.common.iam.entity.CauthI;
import com.kdb.it.common.iam.entity.CorgnI;
import com.kdb.it.common.iam.entity.CroleIId;
import com.kdb.it.common.iam.entity.CuserI;
import com.kdb.it.common.iam.repository.AuthRepository;
import com.kdb.it.common.iam.repository.OrganizationRepository;
import com.kdb.it.common.iam.repository.RoleRepository;
import com.kdb.it.common.iam.repository.UserRepository;
import com.kdb.it.common.system.repository.LoginHistoryRepository;
import com.kdb.it.common.system.repository.RefreshTokenRepository;
import com.kdb.it.infra.file.repository.FileRepository;

/**
 * AdminService 단위 테스트
 *
 * <p>
 * Mockito로 모든 Repository를 Mock 처리하여 Oracle DB 없이
 * 관리자 CRUD 비즈니스 로직(공통코드, 자격등급, 역할, 사용자, 조직)을 검증합니다.
 * </p>
 */
@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private CodeRepository codeRepository;
    @Mock
    private AuthRepository authRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private OrganizationRepository orgRepository;
    @Mock
    private LoginHistoryRepository loginHistoryRepository;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private FileRepository fileRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AdminService adminService;

    // =========================================================================
    // 공통코드 (Ccodem)
    // =========================================================================

    @Test
    @DisplayName("createCode - 중복 코드ID 존재 시 IllegalArgumentException 발생")
    void createCode_중복코드ID_예외발생() {
        // given: 이미 존재하는 코드ID
        LocalDate sttDt = LocalDate.of(2026, 1, 1);
        AdminDto.CodeRequest req = new AdminDto.CodeRequest(
                "CODE001", "코드명", "값", "설명", "구분", "구분설명", sttDt, null, 1);
        given(codeRepository.existsByCdIdAndSttDt("CODE001", sttDt)).willReturn(true);

        // when & then
        assertThatThrownBy(() -> adminService.createCode(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("이미 존재하는 코드ID/시작일자입니다");
    }

    @Test
    @DisplayName("createCode - 정상 요청 시 codeRepository.save() 호출")
    void createCode_정상요청_저장호출() {
        // given
        LocalDate sttDt = LocalDate.of(2026, 1, 1);
        AdminDto.CodeRequest req = new AdminDto.CodeRequest(
                "CODE002", "코드명", "값", "설명", "구분", "구분설명", sttDt, null, 1);
        given(codeRepository.existsByCdIdAndSttDt("CODE002", sttDt)).willReturn(false);

        // when
        adminService.createCode(req);

        // then
        verify(codeRepository, times(1)).save(any(Ccodem.class));
    }

    @Test
    @DisplayName("updateCode - 미존재 코드ID 수정 시 IllegalArgumentException 발생")
    void updateCode_미존재코드ID_예외발생() {
        // given
        LocalDate sttDt = LocalDate.of(2026, 1, 1);
        AdminDto.CodeRequest req = new AdminDto.CodeRequest(
                "NONE", "코드명", "값", "설명", "구분", "구분설명", sttDt, null, 1);
        given(codeRepository.findByCdIdAndSttDtAndDelYn("NONE", sttDt, "N")).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> adminService.updateCode("NONE", sttDt, req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("존재하지 않는 코드ID/시작일자입니다");
    }

    @Test
    @DisplayName("deleteCode - 정상 삭제 시 code.delete() 호출 (Soft Delete)")
    void deleteCode_정상삭제_SoftDelete() {
        // given
        LocalDate sttDt = LocalDate.of(2026, 1, 1);
        Ccodem code = Ccodem.builder().cdId("CODE001").sttDt(sttDt).build();
        given(codeRepository.findByCdIdAndSttDtAndDelYn("CODE001", sttDt, "N")).willReturn(Optional.of(code));

        // when
        adminService.deleteCode("CODE001", sttDt);

        // then: DEL_YN='Y' 처리 검증
        assertThat(code.getDelYn()).isEqualTo("Y");
    }

    @Test
    @DisplayName("bulkUpsertCodes - 신규/수정 건수를 정확히 반환한다")
    void bulkUpsertCodes_신규수정건수반환() {
        // given: CODE001은 기존 존재, CODE002는 신규
        LocalDate sttDt = LocalDate.of(2026, 1, 1);
        AdminDto.CodeRequest req1 = new AdminDto.CodeRequest("CODE001", "코드1", null, null, null, null, sttDt, null, 1);
        AdminDto.CodeRequest req2 = new AdminDto.CodeRequest("CODE002", "코드2", null, null, null, null, sttDt, null, 2);
        AdminDto.BulkCodeRequest bulkReq = new AdminDto.BulkCodeRequest(List.of(req1, req2));

        Ccodem existingCode = Ccodem.builder().cdId("CODE001").sttDt(sttDt).build();
        given(codeRepository.existsByCdIdAndSttDt("CODE001", sttDt)).willReturn(true);
        given(codeRepository.findByCdIdAndSttDtAndDelYn("CODE001", sttDt, "N")).willReturn(Optional.of(existingCode));
        given(codeRepository.existsByCdIdAndSttDt("CODE002", sttDt)).willReturn(false);

        // when
        var result = adminService.bulkUpsertCodes(bulkReq);

        // then
        assertThat(result.get("updated")).isEqualTo(1);
        assertThat(result.get("created")).isEqualTo(1);
        verify(codeRepository, times(1)).save(any(Ccodem.class));
    }

    // =========================================================================
    // 자격등급 (CauthI)
    // =========================================================================

    @Test
    @DisplayName("createAuthGrade - 중복 ATH_ID 존재 시 IllegalArgumentException 발생")
    void createAuthGrade_중복ID_예외발생() {
        // given
        AdminDto.AuthGradeRequest req = new AdminDto.AuthGradeRequest("ITPAD001", "관리자", "관리자 자격", "Y");
        given(authRepository.existsById("ITPAD001")).willReturn(true);

        // when & then
        assertThatThrownBy(() -> adminService.createAuthGrade(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("이미 존재하는 자격등급ID입니다");
    }

    @Test
    @DisplayName("createAuthGrade - 정상 요청 시 authRepository.save() 호출")
    void createAuthGrade_정상요청_저장호출() {
        // given
        AdminDto.AuthGradeRequest req = new AdminDto.AuthGradeRequest("ITPNEW", "신규등급", "신규 자격", "Y");
        given(authRepository.existsById("ITPNEW")).willReturn(false);

        // when
        adminService.createAuthGrade(req);

        // then
        verify(authRepository, times(1)).save(any(CauthI.class));
    }

    @Test
    @DisplayName("deleteAuthGrade - 정상 삭제 시 Soft Delete (DEL_YN='Y')")
    void deleteAuthGrade_정상삭제_SoftDelete() {
        // given
        CauthI auth = CauthI.builder().athId("ITPZZ001").delYn("N").build();
        given(authRepository.findById("ITPZZ001")).willReturn(Optional.of(auth));

        // when
        adminService.deleteAuthGrade("ITPZZ001");

        // then
        assertThat(auth.getDelYn()).isEqualTo("Y");
    }

    // =========================================================================
    // 역할 (CroleI)
    // =========================================================================

    @Test
    @DisplayName("createRole - 복합키 중복 시 IllegalArgumentException 발생")
    void createRole_복합키중복_예외발생() {
        // given
        AdminDto.RoleRequest req = new AdminDto.RoleRequest("ITPAD001", "10001", "Y");
        CroleIId id = new CroleIId("ITPAD001", "10001");
        given(roleRepository.existsById(id)).willReturn(true);

        // when & then
        assertThatThrownBy(() -> adminService.createRole(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("이미 존재하는 역할입니다");
    }

    @Test
    @DisplayName("deleteRole - 미존재 역할 삭제 시 IllegalArgumentException 발생")
    void deleteRole_미존재역할_예외발생() {
        // given
        CroleIId id = new CroleIId("ITPAD001", "99999");
        given(roleRepository.findById(id)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> adminService.deleteRole("ITPAD001", "99999"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("존재하지 않는 역할입니다");
    }

    // =========================================================================
    // 사용자 (CuserI)
    // =========================================================================

    @Test
    @DisplayName("createUser - 중복 ENO 존재 시 IllegalArgumentException 발생")
    void createUser_중복ENO_예외발생() {
        // given
        AdminDto.UserRequest req = new AdminDto.UserRequest(
                "10001", "홍길동", null, null, null, null, null, null, null);
        given(userRepository.existsByEno("10001")).willReturn(true);

        // when & then
        assertThatThrownBy(() -> adminService.createUser(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("이미 존재하는 사원번호입니다");
    }

    @Test
    @DisplayName("createUser - password가 null이면 기본값 'changeme'로 인코딩 후 저장")
    void createUser_passwordNull_기본값인코딩() {
        // given: password 없는 요청
        AdminDto.UserRequest req = new AdminDto.UserRequest(
                "10002", "김테스트", null, null, null, null, null, null, null);
        given(userRepository.existsByEno("10002")).willReturn(false);
        given(passwordEncoder.encode("changeme")).willReturn("encodedDefault");

        // when
        adminService.createUser(req);

        // then
        verify(passwordEncoder, times(1)).encode("changeme");
        verify(userRepository, times(1)).save(any(CuserI.class));
    }

    @Test
    @DisplayName("deleteUser - 미존재 사용자 삭제 시 IllegalArgumentException 발생")
    void deleteUser_미존재사용자_예외발생() {
        // given
        given(userRepository.findByEno("99999")).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> adminService.deleteUser("99999"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("존재하지 않는 사원번호입니다");
    }

    @Test
    @DisplayName("updateUser - password 포함 시 비밀번호도 함께 변경")
    void updateUser_password포함_비밀번호변경() {
        // given
        CuserI user = CuserI.builder().eno("10001").delYn("N").build();
        AdminDto.UserRequest req = new AdminDto.UserRequest(
                "10001", "홍길동", null, null, null, null, null, null, "newPassword");
        given(userRepository.findByEno("10001")).willReturn(Optional.of(user));
        given(passwordEncoder.encode("newPassword")).willReturn("encodedNew");

        // when
        adminService.updateUser("10001", req);

        // then: 비밀번호 인코딩 호출 확인
        verify(passwordEncoder, times(1)).encode("newPassword");
    }

    // =========================================================================
    // 조직 (CorgnI)
    // =========================================================================

    @Test
    @DisplayName("createOrganization - 중복 조직코드 존재 시 IllegalArgumentException 발생")
    void createOrganization_중복조직코드_예외발생() {
        // given
        AdminDto.OrgRequest req = new AdminDto.OrgRequest("BBR001", "IT부문", "IT Division", "1", null);
        given(orgRepository.existsById("BBR001")).willReturn(true);

        // when & then
        assertThatThrownBy(() -> adminService.createOrganization(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("이미 존재하는 조직코드입니다");
    }

    @Test
    @DisplayName("deleteOrganization - 정상 삭제 시 Soft Delete (DEL_YN='Y')")
    void deleteOrganization_정상삭제_SoftDelete() {
        // given
        // delYn("N") 명시: @PrePersist는 실제 JPA 영속 시에만 호출되므로 테스트에서 직접 설정
        CorgnI org = CorgnI.builder().prlmOgzCCone("BBR001").delYn("N").build();
        given(orgRepository.findById("BBR001")).willReturn(Optional.of(org));

        // when
        adminService.deleteOrganization("BBR001");

        // then
        assertThat(org.getDelYn()).isEqualTo("Y");
    }

    @Test
    @DisplayName("getCodes - 활성 코드 목록을 반환하며 감사 필드를 이름으로 변환한다")
    void getCodes_활성코드목록반환() {
        // given
        Ccodem code = Ccodem.builder().cdId("CODE001").cdNm("코드1").build();
        given(codeRepository.findAllActive()).willReturn(List.of(code));
        given(userRepository.findByEnoIn(any())).willReturn(Collections.emptyList());

        // when
        List<AdminDto.CodeResponse> result = adminService.getCodes();

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).cdId()).isEqualTo("CODE001");
    }
}
