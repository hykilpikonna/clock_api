package org.hydev.clock_api.repository;

import org.hydev.clock_api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    // https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#jpa.query-methods
    boolean existsByUsername(String username);
    User queryByUsername(String username);
}
