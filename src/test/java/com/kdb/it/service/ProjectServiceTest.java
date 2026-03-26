package com.kdb.it.service;

import com.kdb.it.domain.entity.Bprojm;
import com.kdb.it.dto.ProjectDto;
import com.kdb.it.repository.BitemmRepository;
import com.kdb.it.repository.CapplaRepository;
import com.kdb.it.repository.CapplmRepository;
import com.kdb.it.common.iam.repository.OrganizationRepository;
import com.kdb.it.common.iam.repository.UserRepository;
import com.kdb.it.repository.ProjectRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

/**
 * ProjectService 단위 테스트
 *
 * <p>
 * 모든 Repository를 Mock 처리하여 Oracle DB 없이 비즈니스 로직을 검증합니다.
 * 특히 결재중/결재완료 프로젝트 삭제 거부 등 핵심 비즈니스 제약을 검증합니다.
 * </p>
 */
@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

        @Mock
        private ProjectRepository projectRepository;
        @Mock
        private CapplaRepository capplaRepository;
        @Mock
        private CapplmRepository capplmRepository;
        @Mock
        private BitemmRepository bitemmRepository;
        @Mock
        private OrganizationRepository corgnIRepository;
        @Mock
        private UserRepository cuserIRepository;

        @InjectMocks
        private ProjectService projectService;

        @Test
        @DisplayName("getProjectList - DEL_YN=N 프로젝트 목록 반환")
        void getProjectList_전체목록반환() {
                // given
                Bprojm project = Bprojm.builder()
                                .prjMngNo("PRJ-2026-0001").prjSno(1).delYn("N").build();

                given(projectRepository.findAllByDelYn("N")).willReturn(List.of(project));
                // setApplicationInfo 내부의 findBy... 호출 → Mockito 기본값(빈 리스트) 자동 처리
                given(capplaRepository.findByOrcTbCdAndOrcPkVlAndOrcSnoVlOrderByApfRelSnoDesc(
                                anyString(), anyString(), eq(1))).willReturn(List.of());
                given(bitemmRepository.findByPrjMngNoAndPrjSnoAndDelYn(anyString(), eq(1), anyString()))
                                .willReturn(List.of());

                // when
                List<ProjectDto.Response> result = projectService.getProjectList();

                // then
                assertThat(result).hasSize(1);
                assertThat(result.get(0).getPrjMngNo()).isEqualTo("PRJ-2026-0001");
        }

        @Test
        @DisplayName("getProject - 존재하는 프로젝트 관리번호 조회 시 Response 반환")
        void getProject_존재하는프로젝트_반환() {
                // given
                String prjMngNo = "PRJ-2026-0001";
                Bprojm project = Bprojm.builder()
                                .prjMngNo(prjMngNo).prjSno(1).delYn("N").build();

                given(projectRepository.findByPrjMngNoAndDelYn(prjMngNo, "N"))
                                .willReturn(Optional.of(project));
                given(capplaRepository.findByOrcTbCdAndOrcPkVlAndOrcSnoVlOrderByApfRelSnoDesc(
                                anyString(), eq(prjMngNo), eq(1))).willReturn(List.of());
                given(bitemmRepository.findByPrjMngNoAndPrjSnoAndDelYn(prjMngNo, 1, "N"))
                                .willReturn(List.of());

                // when
                ProjectDto.Response result = projectService.getProject(prjMngNo);

                // then
                assertThat(result).isNotNull();
                assertThat(result.getPrjMngNo()).isEqualTo(prjMngNo);
        }

        @Test
        @DisplayName("getProject - 미존재 프로젝트 조회 시 IllegalArgumentException 발생")
        void getProject_미존재프로젝트_예외발생() {
                // given
                given(projectRepository.findByPrjMngNoAndDelYn("INVALID", "N"))
                                .willReturn(Optional.empty());

                // when & then
                assertThatThrownBy(() -> projectService.getProject("INVALID"))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("Project not found");
        }

        @Test
        @DisplayName("deleteProject - 결재중 신청서 존재 시 IllegalStateException 발생")
        void deleteProject_결재중상태_예외발생() {
                // given
                String prjMngNo = "PRJ-2026-0001";
                Bprojm project = Bprojm.builder()
                                .prjMngNo(prjMngNo).prjSno(1).delYn("N").build();

                given(projectRepository.findByPrjMngNoAndDelYn(prjMngNo, "N"))
                                .willReturn(Optional.of(project));
                // 결재중 신청서 존재
                given(capplaRepository.existsByOrcTbCdAndOrcPkVlAndOrcSnoVlAndApfStsIn(
                                eq("BPROJM"), eq(prjMngNo), eq(1), anyList()))
                                .willReturn(true);

                // when & then
                assertThatThrownBy(() -> projectService.deleteProject(prjMngNo))
                                .isInstanceOf(IllegalStateException.class)
                                .hasMessageContaining("결재중이거나 결재완료된 프로젝트는 삭제할 수 없습니다");
        }

        @Test
        @DisplayName("deleteProject - 정상 상태 프로젝트 삭제 시 project.delete() 호출 (delYn=Y)")
        void deleteProject_정상상태_SoftDelete() {
                // given
                String prjMngNo = "PRJ-2026-0001";
                Bprojm project = Bprojm.builder()
                                .prjMngNo(prjMngNo).prjSno(1).delYn("N").build();

                given(projectRepository.findByPrjMngNoAndDelYn(prjMngNo, "N"))
                                .willReturn(Optional.of(project));
                // 결재중 신청서 없음
                given(capplaRepository.existsByOrcTbCdAndOrcPkVlAndOrcSnoVlAndApfStsIn(
                                eq("BPROJM"), eq(prjMngNo), eq(1), anyList()))
                                .willReturn(false);
                given(bitemmRepository.findByPrjMngNoAndPrjSno(prjMngNo, 1)).willReturn(List.of());

                // when
                projectService.deleteProject(prjMngNo);

                // then: Soft Delete 검증
                assertThat(project.getDelYn()).isEqualTo("Y");
        }

        @Test
        @DisplayName("deleteProject - 미존재 프로젝트 삭제 시 IllegalArgumentException 발생")
        void deleteProject_미존재프로젝트_예외발생() {
                // given
                given(projectRepository.findByPrjMngNoAndDelYn("INVALID", "N"))
                                .willReturn(Optional.empty());

                // when & then
                assertThatThrownBy(() -> projectService.deleteProject("INVALID"))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("Project not found");
        }
}
