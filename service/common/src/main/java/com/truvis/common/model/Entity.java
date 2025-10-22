package com.truvis.common.model;

import java.util.Objects;

public abstract class Entity<ID> {
    
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
