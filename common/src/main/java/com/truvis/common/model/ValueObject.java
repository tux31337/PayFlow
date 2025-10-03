package com.truvis.common.model;

public abstract class ValueObject {
    
    @Override
    public abstract boolean equals(Object obj);
    
    @Override
    public abstract int hashCode();
    
    @Override
    public abstract String toString();
}
