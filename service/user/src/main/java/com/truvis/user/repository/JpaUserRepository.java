package com.truvis.user.repository;

import com.truvis.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA Repository 인터페이스
 * - Spring Data JPA가 자동으로 구현체 생성
 */
interface UserJpaRepository extends JpaRepository<User, Long> {
    
    /**
     * 이메일로 사용자 조회
     * - Email은 @Embeddable이라 .value로 접근
     */
    @Query("SELECT u FROM User u WHERE u.email.value = :email")
    Optional<User> findByEmail(@Param("email") String email);
    
    /**
     * 이메일 존재 여부 확인
     */
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u WHERE u.email.value = :email")
    boolean existsByEmail(@Param("email") String email);
}

/**
 * User Repository JPA 구현체
 * - JPA 기술로 구현
 * - 영속성 처리 담당
 */
@Repository
@Slf4j
@RequiredArgsConstructor
public class JpaUserRepository implements UserRepository {

    private final UserJpaRepository jpaRepository;

    @Override
    public Optional<User> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jpaRepository.findByEmail(email);
    }

    @Override
    public User save(User user) {
        User saved = jpaRepository.save(user);
        
        log.debug("사용자 저장: id={}, email={}", 
                saved.getId(), 
                saved.getEmail().getValue());
        
        return saved;
    }

    @Override
    public void delete(User user) {
        jpaRepository.delete(user);
        
        log.debug("사용자 삭제: id={}", user.getId());
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaRepository.existsByEmail(email);
    }
}
