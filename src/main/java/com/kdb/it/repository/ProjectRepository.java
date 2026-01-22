package com.kdb.it.repository;

import com.kdb.it.domain.entity.Project;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepository extends JpaRepository<Project, String> {
    Optional<Project> findByPrjMngNoAndDelYn(String prjMngNo, String delYn);

    List<Project> findAllByDelYn(String delYn);

    @org.springframework.data.jpa.repository.Query(value = "SELECT S_PRJ.NEXTVAL FROM DUAL", nativeQuery = true)
    Long getNextSequenceValue();
}
