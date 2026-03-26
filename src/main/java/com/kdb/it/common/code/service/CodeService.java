package com.kdb.it.common.code.service;

import com.kdb.it.common.code.dto.CodeDto;
import com.kdb.it.common.code.entity.Ccodem;
import com.kdb.it.common.code.repository.CodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 공통코드(Ccodem) 서비스 클래스
 *
 * <p>
 * 공통코드에 대한 비즈니스 로직(조회, 생성, 수정, 삭제)을 처리합니다.
 * </p>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CodeService {

    private final CodeRepository codeRepository;

    /**
     * 공통코드 단건 조회 (코드ID 기준)
     *
     * <p>
     * 주어진 기준일자(targetDate)를 포함하여 유효한 공통코드를 조회합니다.
     * (targetDate가 null이면 현재 시각 기준)
     * </p>
     *
     * @param cdId       조회할 코드ID
     * @param targetDate 기준일자 (Nullable)
     * @return 공통코드 Response DTO
     * @throws IllegalArgumentException 해당 코드ID의 유효한 공통코드가 없는 경우
     */
    public CodeDto.Response getCcodemById(String cdId, LocalDate targetDate) {
        Ccodem ccodem = codeRepository.findByCdIdWithValidDate(cdId, targetDate)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않거나 존재하지 않는 코드ID 입니다: " + cdId));
        return CodeDto.Response.fromEntity(ccodem);
    }

    /**
     * 공통코드 다건 조회 (코드값구분 기준)
     *
     * <p>
     * 주어진 기준일자(targetDate)를 포함하여 유효한 공통코드 목록을 조회합니다.
     * </p>
     *
     * @param cttTp      조회할 코드값구분
     * @param targetDate 기준일자 (Nullable)
     * @return 공통코드 Response DTO 리스트
     */
    public List<CodeDto.Response> getCcodemByCttTp(String cttTp, LocalDate targetDate) {
        List<Ccodem> ccodems = codeRepository.findByCttTpWithValidDate(cttTp, targetDate);
        return ccodems.stream()
                .map(CodeDto.Response::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 공통코드 신규 생성
     *
     * @param request 신규 생성 요청 DTO
     * @return 생성된 코드ID
     * @throws IllegalArgumentException 코드ID가 중복될 경우
     */
    @Transactional
    public String createCcodem(CodeDto.CreateRequest request) {
        if (codeRepository.existsByCdId(request.getCdId())) {
            throw new IllegalArgumentException("이미 존재하는 코드ID 입니다: " + request.getCdId());
        }

        Ccodem ccodem = request.toEntity();
        codeRepository.save(ccodem);
        return ccodem.getCdId();
    }

    /**
     * 공통코드 수정
     *
     * @param cdId    수정할 코드ID
     * @param request 수정 요청 DTO
     * @return 수정된 코드ID
     * @throws IllegalArgumentException 대상 코드ID가 존재하지 않거나 삭제된 경우
     */
    @Transactional
    public String updateCcodem(String cdId, CodeDto.UpdateRequest request) {
        Ccodem ccodem = codeRepository.findByCdIdAndDelYn(cdId, "N")
                .orElseThrow(() -> new IllegalArgumentException("수정할 공통코드를 찾을 수 없습니다: " + cdId));

        ccodem.update(
                request.getCdNm(),
                request.getCdva(),
                request.getCdDes(),
                request.getCttTp(),
                request.getCttTpDes(),
                request.getCdSqn(),
                request.getSttDt(),
                request.getEndDt());

        return ccodem.getCdId();
    }

    /**
     * 공통코드 삭제 (논리적 삭제)
     *
     * <p>
     * DB에서 완전히 삭제하지 않고 DEL_YN 필드를 'Y'로 업데이트합니다.
     * </p>
     *
     * @param cdId 삭제할 코드ID
     * @throws IllegalArgumentException 대상 코드ID가 존재하지 않거나 이미 삭제된 경우
     */
    @Transactional
    public void deleteCcodem(String cdId) {
        Ccodem ccodem = codeRepository.findByCdIdAndDelYn(cdId, "N")
                .orElseThrow(() -> new IllegalArgumentException("삭제할 공통코드를 찾을 수 없거나 이미 삭제되었습니다: " + cdId));

        ccodem.delete(); // BaseEntity의 delete() 호출 -> delYn = 'Y'
    }
}
