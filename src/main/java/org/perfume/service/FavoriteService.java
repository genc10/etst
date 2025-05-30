package org.perfume.service;

import org.perfume.model.dto.response.PerfumeResponse;

import java.util.List;

public interface FavoriteService {
    List<PerfumeResponse> getFavorites(Long userId);
    void addToFavorites(Long userId, Long perfumeId);
    void removeFromFavorites(Long userId, Long perfumeId);
    boolean isFavorite(Long userId, Long perfumeId);
    List<PerfumeResponse> getMostFavoritedPerfumes();
}