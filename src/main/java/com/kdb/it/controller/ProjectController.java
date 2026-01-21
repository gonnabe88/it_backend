package com.kdb.it.controller;

import com.kdb.it.dto.ProjectDto;
import com.kdb.it.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping
    public ResponseEntity<List<ProjectDto.Response>> getProjects() {
        return ResponseEntity.ok(projectService.getProjectList());
    }

    @GetMapping("/{prjMngNo}")
    public ResponseEntity<ProjectDto.Response> getProject(@PathVariable("prjMngNo") String prjMngNo) {
        ProjectDto.Response response = projectService.getProject(prjMngNo);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<String> createProject(@RequestBody ProjectDto.CreateRequest request) {
        String prjMngNo = projectService.createProject(request);
        return ResponseEntity.created(URI.create("/api/projects/" + prjMngNo)).body(prjMngNo);
    }

    @PutMapping("/{prjMngNo}")
    public ResponseEntity<String> updateProject(@PathVariable("prjMngNo") String prjMngNo, @RequestBody ProjectDto.UpdateRequest request) {
        String updatedPrjMngNo = projectService.updateProject(prjMngNo, request);
        return ResponseEntity.ok(updatedPrjMngNo);
    }

    @DeleteMapping("/{prjMngNo}")
    public ResponseEntity<Void> deleteProject(@PathVariable("prjMngNo") String prjMngNo) {
        projectService.deleteProject(prjMngNo);
        return ResponseEntity.noContent().build();
    }
}
