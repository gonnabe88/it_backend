package com.kdb.it.domain.entity;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class CdecimId implements Serializable {
    private String dcdMngNo;
    private Integer dcdSqn;
}
