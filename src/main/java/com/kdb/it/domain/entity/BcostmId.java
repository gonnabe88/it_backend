package com.kdb.it.domain.entity;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class BcostmId implements Serializable {
    private String itMngcNo; // 전산업무비코드(IT관리비관리번호)
    private Integer itMngcSno; // 전산업무비일련번호(IT관리비일련번호)
}
