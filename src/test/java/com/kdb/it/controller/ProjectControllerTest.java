package com.kdb.it.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kdb.it.budget.project.controller.ProjectController;
import com.kdb.it.budget.project.dto.ProjectDto;
import com.kdb.it.budget.project.service.ProjectService;
import com.kdb.it.common.system.security.JwtUtil;
import com.kdb.it.common.system.service.CustomUserDetailsService;
import com.kdb.it.config.JacksonConfig;
import com.kdb.it.config.TestSecurityConfig;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * ProjectController @WebMvcTest
 *
 * <p>
 * Spring Security 인증/인가 동작과 HTTP 응답 구조를 검증합니다.
 * 인증 필요 엔드포인트는 @WithMockUser로 처리합니다.
 * </p>
 */
@WebMvcTest(ProjectController.class)
@Import({ TestSecurityConfig.class, JacksonConfig.class })
class ProjectControllerTest {

        @Autowired
        private MockMvc mockMvc;
        @Autowired
        private ObjectMapper objectMapper;

        @MockitoBean
        private ProjectService projectService;
        @MockitoBean
        private JwtUtil jwtUtil;
        @MockitoBean
        private CustomUserDetailsService customUserDetailsService;

        @Test
        @DisplayName("GET /api/projects - 인증된 사용자 → 200 + 목록 반환")
        @WithMockUser(username = "10001")
        void getProjects_인증된사용자_200반환() throws Exception {
                // given
                ProjectDto.Response project = ProjectDto.Response.builder()
                                .prjMngNo("PRJ-2026-0001")
                                .prjNm("테스트 사업")
                                .build();
                given(projectService.getProjectList()).willReturn(List.of(project));

                // when & then
                mockMvc.perform(get("/api/projects"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].prjMngNo").value("PRJ-2026-0001"))
                                .andExpect(jsonPath("$[0].prjNm").value("테스트 사업"));
        }

        @Test
        @DisplayName("GET /api/projects - 비인증 요청 → 401 반환")
        void getProjects_비인증_401반환() throws Exception {
                mockMvc.perform(get("/api/projects"))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("GET /api/projects/{prjMngNo} - 존재하는 프로젝트 → 200 + 상세 정보")
        @WithMockUser(username = "10001")
        void getProject_존재하는프로젝트_200반환() throws Exception {
                // given
                ProjectDto.Response detail = ProjectDto.Response.builder()
                                .prjMngNo("PRJ-2026-0001")
                                .prjNm("테스트 사업")
                                .build();
                given(projectService.getProject("PRJ-2026-0001")).willReturn(detail);

                // when & then
                mockMvc.perform(get("/api/projects/PRJ-2026-0001"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.prjMngNo").value("PRJ-2026-0001"));
        }

        @Test
        @DisplayName("POST /api/projects - 인증된 사용자 → 201 + Location 헤더")
        @WithMockUser(username = "10001")
        void createProject_성공_201반환() throws Exception {
                // given
                ProjectDto.CreateRequest request = ProjectDto.CreateRequest.builder()
                                .prjNm("신규 사업")
                                .build();
                given(projectService.createProject(any(ProjectDto.CreateRequest.class)))
                                .willReturn("PRJ-2026-0001");

                // when & then
                mockMvc.perform(post("/api/projects")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated())
                                .andExpect(header().string("Location", "/api/projects/PRJ-2026-0001"))
                                .andExpect(content().string("PRJ-2026-0001"));
        }

        @Test
        @DisplayName("DELETE /api/projects/{prjMngNo} - 결재중 프로젝트 삭제 → 500 반환")
        @WithMockUser(username = "10001")
        void deleteProject_결재중_500반환() throws Exception {
                // given
                doThrow(new IllegalStateException("결재중이거나 결재완료된 프로젝트는 삭제할 수 없습니다."))
                                .when(projectService).deleteProject("PRJ-2026-0001");

                // when & then
                mockMvc.perform(delete("/api/projects/PRJ-2026-0001"))
                                .andExpect(status().isBadRequest());
        }
}
