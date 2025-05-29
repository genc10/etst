package org.perfume.domain.repo;

import org.perfume.domain.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteDao extends JpaRepository<Favorite, Long> {
    List<Favorite> findByUserId(Long userId);
    
    @Query("SELECT f FROM Favorite f JOIN FETCH f.perfume WHERE f.user.id = :userId")
    List<Favorite> findByUserIdWithPerfume(@Param("userId") Long userId);
    
    Optional<Favorite> findByUserIdAndPerfumeId(Long userId, Long perfumeId);
    
    boolean existsByUserIdAndPerfumeId(Long userId, Long perfumeId);
    
    void deleteByUserIdAndPerfumeId(Long userId, Long perfumeId);
    
    void deleteByUserId(Long userId);
    
    @Query("SELECT COUNT(f) FROM Favorite f WHERE f.perfume.id = :perfumeId")
    long countByPerfumeId(@Param("perfumeId") Long perfumeId);
    
    @Query("SELECT f.perfume.id, COUNT(f) as favoriteCount FROM Favorite f GROUP BY f.perfume.id ORDER BY favoriteCount DESC")
    List<Object[]> findMostFavoredPerfumes();
}