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
    private final com.kdb.it.repository.CapplaRepository capplaRepository;
    private final com.kdb.it.repository.CapplmRepository capplmRepository;

    public List<ProjectDto.Response> getProjectList() {
        return projectRepository.findAllByDelYn("N").stream()
                .map(project -> {
                    ProjectDto.Response response = ProjectDto.Response.fromEntity(project);
                    setApplicationInfo(response, project.getPrjMngNo(), project.getPrjSno());
                    return response;
                })
                .toList();
    }

    public ProjectDto.Response getProject(String prjMngNo) {
        Project project = projectRepository.findByPrjMngNoAndDelYn(prjMngNo, "N")
                .orElseThrow(() -> new IllegalArgumentException("Project not found with id: " + prjMngNo));
        
        ProjectDto.Response response = ProjectDto.Response.fromEntity(project);
        setApplicationInfo(response, prjMngNo, project.getPrjSno());
        return response;
    }

    // ... (중략) ...



    @Transactional
    public String createProject(ProjectDto.CreateRequest request) {
        String prjMngNo = request.getPrjMngNo();

        if (prjMngNo == null || prjMngNo.isEmpty()) {
            Long nextVal = projectRepository.getNextSequenceValue();
            
            String year = request.getBgYy();
            if (year == null || year.isEmpty()) {
                year = String.valueOf(java.time.LocalDate.now().getYear());
                request.setBgYy(year);
            }
            
            // Format: PRJ-{bgYy}-{sequence} (padded to 4 digits)
            prjMngNo = String.format("PRJ-%s-%04d", year, nextVal);
            request.setPrjMngNo(prjMngNo);
        } else {
             if (projectRepository.existsById(prjMngNo)) {
                throw new IllegalArgumentException("Project already exists with id: " + prjMngNo);
            }
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

    // 일괄 조회 (여러 프로젝트를 한 번에 조회)
    public List<ProjectDto.Response> getProjectsByIds(ProjectDto.BulkGetRequest request) {
        return request.getPrjMngNos().stream()
                .map(prjMngNo -> {
                    try {
                        return getProject(prjMngNo);
                    } catch (IllegalArgumentException e) {
                        // 존재하지 않는 프로젝트는 결과에서 제외
                        return null;
                    }
                })
                .filter(response -> response != null) // null 제거
                .toList();
    }

    private void setApplicationInfo(ProjectDto.Response response, String prjMngNo, Integer prjSno) {
        // TAAABB_BPRJTM 테이블 코드와 프로젝트 관리번호, 순번으로 신청서 조회
        List<com.kdb.it.domain.entity.Cappla> capplas = capplaRepository
                .findByOrcTbCdAndOrcPkVlAndOrcSnoVlOrderByApfRelSnoDesc("BPRJTM", prjMngNo, prjSno);
        
        if (!capplas.isEmpty()) {
            com.kdb.it.domain.entity.Cappla cappla = capplas.get(0); // 가장 최신 신청서
            response.setApfMngNo(cappla.getApfMngNo());
            
            capplmRepository.findById(cappla.getApfMngNo())
                    .ifPresent(capplm -> response.setApfSts(capplm.getApfSts()));
        }
    }
}
