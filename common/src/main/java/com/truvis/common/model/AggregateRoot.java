package com.truvis.common.model;

import java.util.ArrayList;
import java.util.List;

public abstract class AggregateRoot<ID> extends Entity<ID> {
    
    private final List<DomainEvent> domainEvents = new ArrayList<>();
    
    // JPA용 기본 생성자
    protected AggregateRoot() {
        super();
    }
    
    protected AggregateRoot(ID id) {
        super(id);
    }
    
    protected void addDomainEvent(DomainEvent event) {
        this.domainEvents.add(event);
    }
    
    public List<DomainEvent> getDomainEvents() {
        return List.copyOf(domainEvents);
    }
    
    public void clearDomainEvents() {
        this.domainEvents.clear();
    }
}
