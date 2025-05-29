package org.perfume.domain.repo;

import org.perfume.domain.entity.Order;
import org.perfume.model.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderDao extends JpaRepository<Order, Long> {
    List<Order> findByUserId(Long userId);
    List<Order> findByStatus(OrderStatus status);
    Page<Order> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    List<Order> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    List<Order> findByUserIdAndStatus(Long userId, OrderStatus status);
}