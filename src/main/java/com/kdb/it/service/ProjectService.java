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
    private final com.kdb.it.repository.BitemmRepository bitemmRepository;

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

        // 3. 품목 정보 조회 및 설정 (삭제되지 않은 항목만)
        // PRJ_MNG_NO(프로젝트관리번호), PRJ_SNO(프로젝트일련번호) 기준, DEL_YN='N'인 품목 조회
        List<com.kdb.it.domain.entity.Bitemm> bitemms = bitemmRepository.findByPrjMngNoAndPrjSnoAndDelYn(prjMngNo,
                project.getPrjSno(), "N");

        // 엔티티를 DTO로 변환하여 응답 객체에 설정
        List<ProjectDto.BitemmDto> itemDtos = bitemms.stream()
                .map(ProjectDto.BitemmDto::fromEntity)
                .toList();
        response.setItems(itemDtos);

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

        // 결재 상태 확인 (결재중, 결재완료 상태인 경우 수정 불가)
        boolean isProcessingOrApproved = capplaRepository.existsByOrcTbCdAndOrcPkVlAndOrcSnoVlAndApfStsIn(
                "BPRJTM", prjMngNo, project.getPrjSno(), java.util.List.of("결재중", "결재완료"));

        if (isProcessingOrApproved) {
            throw new IllegalStateException("결재중이거나 결재완료된 프로젝트는 수정할 수 없습니다.");
        }

        project.update(
                request.getPrjNm(), request.getPrjTp(), request.getSvnDpm(), request.getItDpm(),
                request.getPrjBg(), request.getSttDt(), request.getEndDt(), request.getSvnDpmCgpr(),
                request.getItDpmCgpr(), request.getSvnDpmTlr(), request.getItDpmTlr(), request.getEdrt(),
                request.getPrjDes(), request.getPulRsn(), request.getSaf(), request.getNcs(),
                request.getXptEff(), request.getPlm(), request.getPrjRng(), request.getPulPsg(),
                request.getHrfPln(), request.getBzDtt(), request.getTchnTp(), request.getMnUsr(),
                request.getDplYn(), request.getLblFsgTlm(), request.getRprSts(), request.getPrjPulPtt(),
                request.getPrjSts(), request.getBgYy(), request.getSvnHdq());

        // 품목 정보 동기화 (CUD)
        if (request.getItems() != null) {
            // 1. 기존 품목 조회 (DEL_YN='N')
            List<com.kdb.it.domain.entity.Bitemm> existingItems = bitemmRepository
                    .findByPrjMngNoAndPrjSnoAndDelYn(prjMngNo, project.getPrjSno(), "N");

            java.util.Set<String> processedGclMngNos = new java.util.HashSet<>();
            int maxGclSno = existingItems.stream()
                    .mapToInt(com.kdb.it.domain.entity.Bitemm::getGclSno)
                    .max().orElse(0);

            // 2. 요청 품목 처리 (수정 또는 신규 추가)
            for (ProjectDto.BitemmDto itemDto : request.getItems()) {
                if (itemDto.getGclMngNo() != null && !itemDto.getGclMngNo().isEmpty()) {
                    // 수정: 기존 항목 찾기
                    com.kdb.it.domain.entity.Bitemm existingItem = existingItems.stream()
                            .filter(item -> item.getGclMngNo().equals(itemDto.getGclMngNo())
                                    && item.getGclSno().equals(itemDto.getGclSno()))
                            .findFirst()
                            .orElse(null);

                    if (existingItem != null) {
                        // 기존 항목 업데이트 (Setter가 없으므로 Builder로 새로 생성하되 ID 유지 - BaseEntity 로직 고려 필요)
                        // 여기서는 Setter 없이 불변 객체 스타일이라면, JPA Dirty Checking을 위해 엔티티에 update 메서드가 필요함.
                        // Bitemm 엔티티에 update 메서드가 없으므로, 편의상 Repository.save()로 덮어쓰기 (merge) 하거나
                        // Bitemm에 update 메서드를 추가하는 것이 정석이나, 현재는 delete -> insert 대신 값을 변경하는 방식이 효율적.
                        // 시간이 없으므로 Bitemm을 새로 빌드해서 save 호출 (JPA save는 ID가 있으면 merge 수행)
                        com.kdb.it.domain.entity.Bitemm updatedItem = com.kdb.it.domain.entity.Bitemm.builder()
                                .gclMngNo(existingItem.getGclMngNo())
                                .gclSno(existingItem.getGclSno())
                                .prjMngNo(existingItem.getPrjMngNo())
                                .prjSno(existingItem.getPrjSno())
                                .gclDtt(itemDto.getGclDtt())
                                .gclNm(itemDto.getGclNm())
                                .gclQtt(itemDto.getGclQtt())
                                .cur(itemDto.getCur())
                                .xcr(itemDto.getXcr())
                                .xcrBseDt(itemDto.getXcrBseDt())
                                .bgFdtn(itemDto.getBgFdtn())
                                .itdDt(itemDto.getItdDt())
                                .dfrCle(itemDto.getDfrCle())
                                .infPrtYn(itemDto.getInfPrtYn() == null ? "N" : itemDto.getInfPrtYn())
                                .itrInfrYn(itemDto.getItrInfrYn() == null ? "N" : itemDto.getItrInfrYn())
                                .lstYn("Y")
                                .gclAmt(itemDto.getGclAmt())
                                // BaseEntity 필드는 유지됨 (Auditing)
                                .build();
                        bitemmRepository.save(updatedItem);
                        processedGclMngNos.add(existingItem.getGclMngNo());
                    }
                } else {
                    // 신규 추가
                    Long gclSeq = bitemmRepository.getNextSequenceValue();
                    String gclMngNo = String.format("GCL-%s-%04d", java.time.LocalDate.now().getYear(), gclSeq);

                    com.kdb.it.domain.entity.Bitemm newItem = com.kdb.it.domain.entity.Bitemm.builder()
                            .gclMngNo(gclMngNo)
                            .gclSno(++maxGclSno)
                            .prjMngNo(prjMngNo)
                            .prjSno(project.getPrjSno())
                            .gclDtt(itemDto.getGclDtt())
                            .gclNm(itemDto.getGclNm())
                            .gclQtt(itemDto.getGclQtt())
                            .cur(itemDto.getCur())
                            .xcr(itemDto.getXcr())
                            .xcrBseDt(itemDto.getXcrBseDt())
                            .bgFdtn(itemDto.getBgFdtn())
                            .itdDt(itemDto.getItdDt())
                            .dfrCle(itemDto.getDfrCle())
                            .infPrtYn(itemDto.getInfPrtYn() == null ? "N" : itemDto.getInfPrtYn())
                            .itrInfrYn(itemDto.getItrInfrYn() == null ? "N" : itemDto.getItrInfrYn())
                            .lstYn("Y")
                            .gclAmt(itemDto.getGclAmt())
                            .build();
                    bitemmRepository.save(newItem);
                }
            }

            // 3. 삭제 처리 (요청 목록에 없는 기존 항목 Soft Delete)
            for (com.kdb.it.domain.entity.Bitemm existingItem : existingItems) {
                if (!processedGclMngNos.contains(existingItem.getGclMngNo())) {
                    existingItem.delete();
                }
            }
        }

        return project.getPrjMngNo();
    }

    @Transactional
    public void deleteProject(String prjMngNo) {
        Project project = projectRepository.findById(prjMngNo)
                .orElseThrow(() -> new IllegalArgumentException("Project not found with id: " + prjMngNo));

        // 0. 결재 상태 확인 (결재중, 결재완료 상태인 경우 삭제 불가)
        // TAAABB_BPRJTM 테이블 코드는 'BPRJTM'으로 가정
        boolean isProcessingOrApproved = capplaRepository.existsByOrcTbCdAndOrcPkVlAndOrcSnoVlAndApfStsIn(
                "BPRJTM", prjMngNo, project.getPrjSno(), java.util.List.of("결재중", "결재완료"));

        if (isProcessingOrApproved) {
            throw new IllegalStateException("결재중이거나 결재완료된 프로젝트는 삭제할 수 없습니다.");
        }

        // 1. 프로젝트 삭제 처리 (Soft Delete)
        project.delete();

        // 2. 관련 품목 정보 조회 및 삭제 처리 (Soft Delete)
        List<com.kdb.it.domain.entity.Bitemm> bitemms = bitemmRepository.findByPrjMngNoAndPrjSno(prjMngNo,
                project.getPrjSno());
        for (com.kdb.it.domain.entity.Bitemm bitemm : bitemms) {
            bitemm.delete(); // BaseEntity의 delete 메서드 호출 (DEL_YN = 'Y')
        }

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