package com.kdb.it.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@MappedSuperclass
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    @Column(name = "DEL_YN", length = 1)
    private String delYn; // 삭제여부

    @Column(name = "GUID", length = 38)
    private String guid; // 일련번호

    @Column(name = "GUID_PRG_SNO")
    private Integer guidPrgSno; // 일련번호2

    @CreatedDate
    @Column(name = "FST_ENR_DTM", updatable = false)
    private LocalDateTime fstEnrDtm; // 최초생성시간

    @CreatedBy
    @Column(name = "FST_ENR_USID", length = 14, updatable = false)
    private String fstEnrUsid; // 최초생성자

    @LastModifiedDate
    @Column(name = "LST_CHG_DTM")
    private LocalDateTime lstChgDtm; // 마지막수정시간

    @LastModifiedBy
    @Column(name = "LST_CHG_USID", length = 14)
    private String lstChgUsid; // 마지막수정자

    @PrePersist
    public void prePersist() {
        if (this.delYn == null) {
            this.delYn = "N";
        }
        if (this.guid == null) {
            this.guid = UUID.randomUUID().toString();
        }
        if (this.guidPrgSno == null) {
            this.guidPrgSno = 1;
        }
    }

    public void delete() {
        this.delYn = "Y";
    }
}
