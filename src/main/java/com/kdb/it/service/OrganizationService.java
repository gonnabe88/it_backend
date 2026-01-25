package com.kdb.it.service;

import java.util.List;
import java.util.stream.Collectors;

import com.kdb.it.dto.OrganizationDto;
import com.kdb.it.repository.CorgnIRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrganizationService {

    private final CorgnIRepository corgnIRepository;

    public List<OrganizationDto.Response> getOrganizations() {
        return corgnIRepository.findAll().stream()
                .map(OrganizationDto.Response::fromEntity)
                .collect(Collectors.toList());
    }
}
