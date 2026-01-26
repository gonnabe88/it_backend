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
public class BitemmId implements Serializable {
    private String gclMngNo; // 품목관리번호
    private Integer gclSno; // 품목일련번호
}
