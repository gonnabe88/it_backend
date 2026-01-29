package com.kdb.it.service;

import com.kdb.it.domain.entity.Bcostm;
import com.kdb.it.dto.CostDto;
import com.kdb.it.repository.BcostmRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CostService {

    private final BcostmRepository bcostmRepository;

    // 특정 전산관리비 조회
    public CostDto.Response getCost(String itMngcNo) {
        // IT_MNGC_NO로 조회하되, 여러 건이 나올 수 있으므로(SNO 차이), 가장 최근 것 또는 목록을 리턴해야 하나?
        // API 명세는 GET /api/cost/{itMngcNo} 로 단건 조회처럼 보임.
        // 하지만 PK는 (IT_MNGC_NO, IT_MNGC_SNO)임.
        // 보통 이런 경우 대표 항목 하나를 조회하거나, 목록을 조회함.
        // 여기서는 IT_MNGC_NO에 해당하는 목록 중 첫번째(또는 특정 로직)를 가져오거나,
        // API 설계상 IT_MNGC_NO가 유니크하다고 가정할 수도 있음.
        // 하지만 테이블 정의상 PK가 복합키이므로 1:N 관계 가능성 있음.
        // 우선 IT_MNGC_NO로 조회되는 목록 중 첫 번째 항목을 반환하도록 구현 (임시)
        // 만약 IT_MNGC_NO가 유니크하다면 리스트 사이즈는 1일 것임.

        List<Bcostm> costs = bcostmRepository.findByItMngcNoAndDelYn(itMngcNo, "N");
        if (costs.isEmpty()) {
            throw new IllegalArgumentException("Cost not found with id: " + itMngcNo);
        }
        // 편의상 첫 번째 항목 반환
        return CostDto.Response.fromEntity(costs.get(0));
    }

    // 전체 전산관리비 조회
    public List<CostDto.Response> getCostList() {
        return bcostmRepository.findAllByDelYn("N").stream()
                .map(CostDto.Response::fromEntity)
                .toList();
    }

    // 신규 전산관리비 생성
    @Transactional
    public String createCost(CostDto.CreateRequest request) {
        // IT_MNGC_NO 생성 로직 (필요시)
        // 여기서는 입력받은 IT_MNGC_NO를 사용하거나, 없으면 생성하는 로직을 추가할 수 있음.
        // 예시: "COST-" + YYYY + SEQ

        String itMngcNo = request.getItMngcNo();
        if (itMngcNo == null || itMngcNo.isEmpty()) {
            Long seq = bcostmRepository.getNextSequenceValue();
            String year = String.valueOf(java.time.LocalDate.now().getYear());
            // Format: COST_{year}_{seq} (padded to 4 digits)
            itMngcNo = String.format("COST_%s_%04d", year, seq);
            request.setItMngcNo(itMngcNo);
        }

        // SNO 채번 (기존 데이터가 있으면 MAX + 1, 없으면 1)
        Integer nextSno = bcostmRepository.getNextSnoValue(itMngcNo);
        if (nextSno == null) {
            nextSno = 1;
        }

        Bcostm bcostm = request.toEntity(nextSno);
        bcostmRepository.save(bcostm);

        return bcostm.getItMngcNo();
    }

    // 전산관리비 수정
    @Transactional
    public String updateCost(String itMngcNo, CostDto.UpdateRequest request) {
        // IT_MNGC_NO로 조회.
        // 수정 시 어떤 SNO를 수정할지 명확하지 않음.
        // API 명세에 SNO가 없음. -> IT_MNGC_NO에 해당하는 모든 항목을 수정? 아니면 특정 항목?
        // 보통 이런 경우 IT_MNGC_NO가 식별자 역할을 한다고 가정.
        // 여기서는 IT_MNGC_NO로 조회된 첫 번째 항목을 수정하거나,
        // 비즈니스 로직에 따라 다름.
        // 우선 IT_MNGC_NO로 조회된 모든 항목(보통 1개라 가정)을 업데이트하거나,
        // 가장 최근 항목(LST_YN='Y')을 업데이트하는 것이 일반적임.

        List<Bcostm> costs = bcostmRepository.findByItMngcNoAndDelYn(itMngcNo, "N");
        if (costs.isEmpty()) {
            throw new IllegalArgumentException("Cost not found with id: " + itMngcNo);
        }

        // LST_YN = 'Y' 인 항목 찾기
        Bcostm target = costs.stream()
                .filter(c -> "Y".equals(c.getLstYn()))
                .findFirst()
                .orElse(costs.get(0)); // 없으면 첫번째

        target.update(
                request.getIoeNm(), request.getCttNm(), request.getCttTp(), request.getCttOpp(),
                request.getItMngcBg(), request.getDfrCle(), request.getFstDfrDt(), request.getCur(),
                request.getXcr(), request.getXcrBseDt(), request.getInfPrtYn(), request.getIndRsn(),
                request.getPulCgpr());

        return target.getItMngcNo();
    }

    // 전산관리비 삭제
    @Transactional
    public void deleteCost(String itMngcNo) {
        List<Bcostm> costs = bcostmRepository.findByItMngcNoAndDelYn(itMngcNo, "N");
        if (costs.isEmpty()) {
            throw new IllegalArgumentException("Cost not found with id: " + itMngcNo);
        }

        for (Bcostm cost : costs) {
            cost.delete();
        }
    }

    // 전산관리비 일괄 조회
    public List<CostDto.Response> getCostsByIds(CostDto.BulkGetRequest request) {
        return request.getItMngcNos().stream()
                .map(itMngcNo -> {
                    try {
                        return getCost(itMngcNo);
                    } catch (IllegalArgumentException e) {
                        return null;
                    }
                })
                .filter(response -> response != null)
                .toList();
    }
}
