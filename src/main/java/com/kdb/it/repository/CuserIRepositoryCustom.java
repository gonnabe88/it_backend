package com.kdb.it.repository;

import com.kdb.it.domain.entity.CuserI;

import java.util.List;

public interface CuserIRepositoryCustom {
    List<CuserI> searchByName(String name);
}
