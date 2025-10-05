package com.truvis.user.domain;

import java.util.Optional;

public interface UserRepository {
    Optional<User> findByEmail(String email);
    User save(User user);
    void delete(User user);
    boolean existsByEmail(String email);
}
