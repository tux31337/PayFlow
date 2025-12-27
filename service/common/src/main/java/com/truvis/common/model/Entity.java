package com.truvis.common.model;

import com.truvis.common.entity.BaseEntity;

import java.util.Objects;

/**
 * 도메인 엔티티 기본 클래스
 * 
 * - BaseEntity 상속: createdAt, updatedAt 자동 관리
 * - ID 기반 동등성 비교
 */
public abstract class Entity<ID> extends BaseEntity {
    
    protected ID id;
    
    // JPA용 기본 생성자
    protected Entity() {
    }
    
    protected Entity(ID id) {
        this.id = Objects.requireNonNull(id, "ID cannot be null");
    }
    
    public ID getId() {
        return id;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Entity<?> entity = (Entity<?>) obj;
        return Objects.equals(id, entity.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
