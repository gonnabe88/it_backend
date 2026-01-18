package com.kdb.it.service;

import com.kdb.it.domain.entity.CuserI;
import com.kdb.it.dto.AuthDto;
import com.kdb.it.repository.CuserIRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final CuserIRepository cuserIRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void signup(AuthDto.SignupRequest request) {
        if (cuserIRepository.existsByEno(request.getEno())) {
            throw new RuntimeException("이미 존재하는 사번입니다.");
        }

        CuserI user = CuserI.builder()
                .eno(request.getEno())
                .usrNm(request.getEmpNm())
                .usrEcyPwd(passwordEncoder.encode(request.getPassword()))
                .delYn("N")
                .fstEnrDtm(LocalDateTime.now())
                .lstChgDtm(LocalDateTime.now())
                .fstEnrUsid(request.getEno()) // Self-registration
                .lstChgUsid(request.getEno())
                .build();

        cuserIRepository.save(user);
    }

    @Transactional(readOnly = true)
    public String getUserName(String eno) {
        return cuserIRepository.findByEno(eno)
                .map(CuserI::getUsrNm)
                .orElse("Unknown");
    }

    // Login is handled by Spring Security, but verification can be added here if
    // needed manually.
}
