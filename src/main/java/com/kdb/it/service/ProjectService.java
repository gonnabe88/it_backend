package com.kdb.it.service;

import com.kdb.it.domain.entity.Project;
import com.kdb.it.dto.ProjectDto;
import com.kdb.it.repository.ProjectRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectService {

    private final ProjectRepository projectRepository;

    public List<ProjectDto.Response> getProjectList() {
        return projectRepository.findAllByDelYn("N").stream()
                .map(ProjectDto.Response::fromEntity)
                .toList();
    }

    public ProjectDto.Response getProject(String prjMngNo) {
        Project project = projectRepository.findByPrjMngNoAndDelYn(prjMngNo, "N")
                .orElseThrow(() -> new IllegalArgumentException("Project not found with id: " + prjMngNo));
        return ProjectDto.Response.fromEntity(project);
    }

    @Transactional
    public String createProject(ProjectDto.CreateRequest request) {
        if (projectRepository.existsById(request.getPrjMngNo())) {
            throw new IllegalArgumentException("Project already exists with id: " + request.getPrjMngNo());
        }
        Project project = request.toEntity();
        projectRepository.save(project);
        return project.getPrjMngNo();
    }

    @Transactional
    public String updateProject(String prjMngNo, ProjectDto.UpdateRequest request) {
        Project project = projectRepository.findById(prjMngNo)
                .orElseThrow(() -> new IllegalArgumentException("Project not found with id: " + prjMngNo));

        project.update(
                request.getPrjNm(), request.getPrjTp(), request.getSvnDpm(), request.getItDpm(),
                request.getPrjBg(), request.getSttDt(), request.getEndDt(), request.getSvnDpmCgpr(),
                request.getItDpmCgpr(), request.getSvnDpmTlr(), request.getItDpmTlr(), request.getEdrt(),
                request.getPrjDes(), request.getPulRsn(), request.getSaf(), request.getNcs(),
                request.getXptEff(), request.getPlm(), request.getPrjRng(), request.getPulPsg(),
                request.getHrfPln(), request.getBzDtt(), request.getTchnTp(), request.getMnUsr(),
                request.getDplYn(), request.getLblFsgTlm(), request.getRprSts(), request.getPrjPulPtt(),
                request.getPrjSts(), request.getBgYy(), request.getSvnHdq());

        return project.getPrjMngNo();
    }

    @Transactional
    public void deleteProject(String prjMngNo) {
        Project project = projectRepository.findById(prjMngNo)
                .orElseThrow(() -> new IllegalArgumentException("Project not found with id: " + prjMngNo));
        project.delete();
    }
}
