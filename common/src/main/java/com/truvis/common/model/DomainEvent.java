package com.truvis.common.model;

import java.time.LocalDateTime;

public abstract class DomainEvent {
    
    private final LocalDateTime occurredOn;
    
    protected DomainEvent() {
        this.occurredOn = LocalDateTime.now();
    }
    
    public LocalDateTime getOccurredOn() {
        return occurredOn;
    }
}
