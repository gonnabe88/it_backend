package com.kdb.it.service;

import com.kdb.it.domain.entity.CuserI;
import com.kdb.it.dto.UserDto;
import com.kdb.it.repository.CuserIRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final CuserIRepository cuserIRepository;

    public List<UserDto.ListResponse> getUsersByOrganization(String orgCode) {
        List<CuserI> users = cuserIRepository.findByBbrC(orgCode);

        // Name is fetched via relationship
        return users.stream()
                .map(user -> UserDto.ListResponse.fromEntity(user, user.getBbrNm()))
                .collect(Collectors.toList());
    }

    public UserDto.DetailResponse getUser(String eno) {
        CuserI user = cuserIRepository.findByEno(eno)
                .orElseThrow(() -> new IllegalArgumentException("User not found with eno: " + eno));

        return UserDto.DetailResponse.fromEntity(user, user.getBbrNm());
    }
}
