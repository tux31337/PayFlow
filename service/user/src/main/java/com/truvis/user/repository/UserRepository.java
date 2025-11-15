package com.truvis.user.repository;

import com.truvis.user.domain.User;

import java.util.Optional;

/**
 * User Repository 인터페이스
 * - 사용자 저장소 추상화
 * - 구현체: JpaUserRepository (JPA)
 */
public interface UserRepository {
    
    /**
     * ID로 사용자 조회
     */
    Optional<User> findById(Long id);
    
    /**
     * 이메일로 사용자 조회
     */
    Optional<User> findByEmail(String email);
    
    /**
     * 사용자 저장
     */
    User save(User user);
    
    /**
     * 사용자 삭제
     */
    void delete(User user);
    
    /**
     * 이메일 존재 여부 확인
     */
    boolean existsByEmail(String email);
}
