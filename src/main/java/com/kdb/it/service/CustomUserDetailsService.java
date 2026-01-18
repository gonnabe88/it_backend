package com.kdb.it.service;

import com.kdb.it.domain.entity.CuserI;
import com.kdb.it.repository.CuserIRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final CuserIRepository cuserIRepository;

    @Override
    public UserDetails loadUserByUsername(String eno) throws UsernameNotFoundException {
        CuserI user = cuserIRepository.findByEno(eno)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + eno));

        return User.builder()
                .username(user.getEno())
                .password(user.getUsrEcyPwd())
                .authorities(Collections.emptyList()) // Roles can be added here
                .build();
    }
}
