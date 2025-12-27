package com.truvis.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 🏛️ 엔티티 공통 필드 추상 클래스
 * 
 * - createdAt: 생성일시 (자동 설정, 수정 불가)
 * - updatedAt: 수정일시 (자동 갱신)
 * 
 * 사용법:
 * - 엔티티 클래스에서 extends BaseEntity 선언
 * - @EnableJpaAuditing 설정 필요 (Application 또는 JpaConfig 클래스)
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
public abstract class BaseEntity {

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

