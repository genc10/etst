package org.perfume.domain.repo;

import org.perfume.domain.entity.User;
import org.perfume.model.enums.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserDao extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    
    boolean existsByEmail(String email);
    
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.cart WHERE u.email = :email")
    Optional<User> findByEmailWithCart(@Param("email") String email);
    
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.favorites WHERE u.id = :id")
    Optional<User> findByIdWithFavorites(@Param("id") Long id);
    
    List<User> findByRole(UserRole role);
    
    Optional<User> findByEmailAndIsGoogleUserTrue(String email);
    
    @Query("SELECT u FROM User u WHERE lower(u.name) LIKE lower(concat('%', :name, '%'))")
    List<User> findByNameContainingIgnoreCase(@Param("name") String name);
    
    Optional<User> findByPhoneNumber(String phoneNumber);
    
    boolean existsByPhoneNumber(String phoneNumber);
}