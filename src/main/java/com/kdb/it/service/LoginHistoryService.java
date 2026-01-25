package com.kdb.it.service;

import com.kdb.it.domain.entity.LoginHistory;
import com.kdb.it.dto.LoginHistoryDto;
import com.kdb.it.repository.LoginHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 로그인 이력 서비스
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LoginHistoryService {

    private final LoginHistoryRepository loginHistoryRepository;

    /**
     * 사용자의 로그인 이력 조회 (최신순, 최대 50개)
     */
    public List<LoginHistoryDto.Response> getLoginHistory(String eno) {
        List<LoginHistory> histories = loginHistoryRepository.findByEnoOrderByLoginTimeDesc(eno);
        
        // 최대 50개로 제한
        if (histories.size() > 50) {
            histories = histories.subList(0, 50);
        }
        
        return LoginHistoryDto.Response.fromEntities(histories);
    }

    /**
     * 최근 10개 로그인 이력 조회
     */
    public List<LoginHistoryDto.Response> getRecentLoginHistory(String eno) {
        List<LoginHistory> histories = loginHistoryRepository.findTop10ByEnoOrderByLoginTimeDesc(eno);
        return LoginHistoryDto.Response.fromEntities(histories);
    }
}
