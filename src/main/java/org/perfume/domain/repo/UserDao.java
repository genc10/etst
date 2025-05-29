package org.perfume.domain.repo;

import org.perfume.domain.entity.User;
import org.perfume.model.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserDao extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    List<User> findByRole(UserRole role);
    Optional<User> findByEmailAndIsGoogleUserTrue(String email);
}