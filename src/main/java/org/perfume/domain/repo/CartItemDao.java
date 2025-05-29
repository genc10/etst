package org.perfume.domain.repo;

import org.perfume.domain.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemDao extends JpaRepository<CartItem, Long> {
    List<CartItem> findByCartId(Long cartId);
    
    Optional<CartItem> findByCartIdAndPerfumeId(Long cartId, Long perfumeId);
    
    @Query("SELECT ci FROM CartItem ci JOIN FETCH ci.perfume WHERE ci.cart.id = :cartId")
    List<CartItem> findByCartIdWithPerfume(@Param("cartId") Long cartId);
    
    void deleteByCartIdAndPerfumeId(Long cartId, Long perfumeId);
    
    void deleteByCartId(Long cartId);
    
    @Query("SELECT COUNT(ci) FROM CartItem ci WHERE ci.cart.user.id = :userId")
    int countByUserId(@Param("userId") Long userId);
    
    boolean existsByCartIdAndPerfumeId(Long cartId, Long perfumeId);
}