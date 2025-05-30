package org.perfume.service.impl;

import lombok.RequiredArgsConstructor;
import org.perfume.domain.entity.Favorite;
import org.perfume.domain.entity.Perfume;
import org.perfume.domain.entity.User;
import org.perfume.domain.repo.FavoriteDao;
import org.perfume.domain.repo.PerfumeDao;
import org.perfume.domain.repo.UserDao;
import org.perfume.exception.NotFoundException;
import org.perfume.mapper.PerfumeMapper;
import org.perfume.model.dto.response.PerfumeResponse;
import org.perfume.service.FavoriteService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class FavoriteServiceImpl implements FavoriteService {

    private final FavoriteDao favoriteDao;
    private final UserDao userDao;
    private final PerfumeDao perfumeDao;
    private final PerfumeMapper perfumeMapper;

    @Override
    @Transactional(readOnly = true)
    public List<PerfumeResponse> getFavorites(Long userId) {
        return favoriteDao.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(favorite -> perfumeMapper.toDto(favorite.getPerfume()))
                .collect(Collectors.toList());
    }

    @Override
    public void addToFavorites(Long userId, Long perfumeId) {
        User user = userDao.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        
        Perfume perfume = perfumeDao.findById(perfumeId)
                .orElseThrow(() -> new NotFoundException("Perfume not found"));

        if (!favoriteDao.existsByUserIdAndPerfumeId(userId, perfumeId)) {
            Favorite favorite = new Favorite();
            favorite.setUser(user);
            favorite.setPerfume(perfume);
            favoriteDao.save(favorite);
        }
    }

    @Override
    public void removeFromFavorites(Long userId, Long perfumeId) {
        favoriteDao.deleteByUserIdAndPerfumeId(userId, perfumeId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isFavorite(Long userId, Long perfumeId) {
        return favoriteDao.existsByUserIdAndPerfumeId(userId, perfumeId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PerfumeResponse> getMostFavoritedPerfumes() {
        return favoriteDao.findMostFavoritedProducts().stream()
                .map(result -> {
                    Long perfumeId = (Long) result[0];
                    return perfumeDao.findById(perfumeId)
                            .map(perfumeMapper::toDto)
                            .orElse(null);
                })
                .filter(response -> response != null)
                .collect(Collectors.toList());
    }
}