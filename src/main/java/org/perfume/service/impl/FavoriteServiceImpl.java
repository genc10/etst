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
                .map(favorite -> {
                    PerfumeResponse response = perfumeMapper.toDto(favorite.getPerfume());
                    response.setFavorite(true);
                    return response;
                })
                .collect(Collectors.toList());
    }

    @Override
    public void addToFavorites(Long userId, Long productId) {
        if (favoriteDao.existsByUserIdAndPerfumeId(userId, productId)) {
            return;
        }

        User user = userDao.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        Perfume perfume = perfumeDao.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found"));

        Favorite favorite = new Favorite();
        favorite.setUser(user);
        favorite.setPerfume(perfume);
        favoriteDao.save(favorite);
    }

    @Override
    public void removeFromFavorites(Long userId, Long productId) {
        favoriteDao.deleteByUserIdAndPerfumeId(userId, productId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isFavorite(Long userId, Long productId) {
        return favoriteDao.existsByUserIdAndPerfumeId(userId, productId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PerfumeResponse> getFavoritesByBrand(Long userId, Long brandId) {
        return favoriteDao.findUserFavoritesByBrand(userId, brandId).stream()
                .map(favorite -> {
                    PerfumeResponse response = perfumeMapper.toDto(favorite.getPerfume());
                    response.setFavorite(true);
                    return response;
                })
                .collect(Collectors.toList());
    }
}