package com.truvis.member.repository;

import com.truvis.member.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * 이메일로 사용자 조회
     */
    Optional<User> findByEmail(String email);
    
    /**
     * 이메일 중복 체크
     */
    boolean existsByEmail(String email);
    
    /**
     * 소셜 ID와 제공자로 사용자 조회 (소셜 로그인용)
     */
    @Query("SELECT u FROM User u WHERE u.socialId = :socialId AND u.socialProvider = :socialProvider")
    Optional<User> findBySocialIdAndSocialProvider(@Param("socialId") String socialId, 
                                                  @Param("socialProvider") String socialProvider);
    
    /**
     * 소셜 사용자 중복 체크
     */
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u WHERE u.socialId = :socialId AND u.socialProvider = :socialProvider")
    boolean existsBySocialIdAndSocialProvider(@Param("socialId") String socialId, 
                                            @Param("socialProvider") String socialProvider);
}
