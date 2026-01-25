package com.kdb.it.controller;

import java.net.URI;
import java.util.List;

import com.kdb.it.dto.ProjectDto;
import com.kdb.it.service.ProjectService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@Tag(name = "Project", description = "정보화사업 API")
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping
    @Operation(summary = "전체 정보화사업 조회", description = "모든 정보화사업 목록을 조회합니다.")
    public ResponseEntity<List<ProjectDto.Response>> getProjects() {
        return ResponseEntity.ok(projectService.getProjectList());
    }

    @GetMapping("/{prjMngNo}")
    @Operation(summary = "특정 정보화사업 조회", description = "특정 정보화사업을 조회합니다.")
    public ResponseEntity<ProjectDto.Response> getProject(@PathVariable("prjMngNo") String prjMngNo) {
        ProjectDto.Response response = projectService.getProject(prjMngNo);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @Operation(summary = "신규 정보화사업 생성", description = "신규 정보화사업을 생성합니다.")
    public ResponseEntity<String> createProject(@RequestBody ProjectDto.CreateRequest request) {
        String prjMngNo = projectService.createProject(request);
        return ResponseEntity.created(URI.create("/api/projects/" + prjMngNo)).body(prjMngNo);
    }

    @PutMapping("/{prjMngNo}")
    @Operation(summary = "정보화사업 수정", description = "정보화사업을 수정합니다.")
    public ResponseEntity<String> updateProject(@PathVariable("prjMngNo") String prjMngNo, @RequestBody ProjectDto.UpdateRequest request) {
        String updatedPrjMngNo = projectService.updateProject(prjMngNo, request);
        return ResponseEntity.ok(updatedPrjMngNo);
    }

    @DeleteMapping("/{prjMngNo}")
    @Operation(summary = "정보화사업 삭제", description = "정보화사업을 삭제합니다.")
    public ResponseEntity<Void> deleteProject(@PathVariable("prjMngNo") String prjMngNo) {
        projectService.deleteProject(prjMngNo);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/bulk-get")
    @Operation(summary = "정보화사업 일괄 조회", description = "여러 개의 정보화사업을 한 번에 조회합니다. 존재하지 않는 프로젝트는 결과에서 제외됩니다.")
    public ResponseEntity<List<ProjectDto.Response>> bulkGetProjects(
            @RequestBody ProjectDto.BulkGetRequest request) {
        List<ProjectDto.Response> responses = projectService.getProjectsByIds(request);
        return ResponseEntity.ok(responses);
    }
}
