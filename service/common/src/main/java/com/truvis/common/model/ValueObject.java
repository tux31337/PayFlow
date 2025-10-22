package com.truvis.common.model;


/**
 * 값 객체(Value Object) 마커 인터페이스
 *
 * 특징:
 * - 식별자(ID)가 없음
 * - 값 자체로 동등성 판단
 * - 불변(Immutable)
 *
 * 구현 시 주의사항:
 * - equals(), hashCode() 반드시 구현 (Lombok @EqualsAndHashCode 권장)
 * - toString() 구현 권장 (Lombok @ToString 권장)
 * - setter 없이 불변 객체로 설계
 */
public interface ValueObject {
    

}
