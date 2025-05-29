package org.perfume.service;

import org.perfume.model.dto.response.PerfumeResponse;

import java.util.List;

public interface FavoriteService {
    List<PerfumeResponse> getFavorites(Long userId);
    void addToFavorites(Long userId, Long productId);
    void removeFromFavorites(Long userId, Long productId);
    boolean isFavorite(Long userId, Long productId);
    List<PerfumeResponse> getFavoritesByBrand(Long userId, Long brandId);
}