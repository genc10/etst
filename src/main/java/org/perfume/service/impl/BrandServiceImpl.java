package org.perfume.service.impl;

import lombok.RequiredArgsConstructor;
import org.perfume.domain.entity.Brand;
import org.perfume.domain.repo.BrandDao;
import org.perfume.exception.AlreadyExistsException;
import org.perfume.exception.NotFoundException;
import org.perfume.mapper.BrandMapper;
import org.perfume.model.dto.request.BrandRequest;
import org.perfume.model.dto.response.BrandResponse;
import org.perfume.service.BrandService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class BrandServiceImpl implements BrandService {

    private final BrandDao brandDao;
    private final BrandMapper brandMapper;

    @Override
    public BrandResponse createBrand(BrandRequest request) {
        if (brandDao.existsByName(request.getName())) {
            throw new AlreadyExistsException("Brand with name " + request.getName() + " already exists");
        }

        Brand brand = brandMapper.toEntity(request);
        return brandMapper.toDto(brandDao.save(brand));
    }

    @Override
    public BrandResponse updateBrand(Long id, BrandRequest request) {
        Brand brand = brandDao.findById(id)
                .orElseThrow(() -> new NotFoundException("Brand not found"));

        if (!brand.getName().equals(request.getName()) && brandDao.existsByName(request.getName())) {
            throw new AlreadyExistsException("Brand with name " + request.getName() + " already exists");
        }

        brand.setName(request.getName());
        brand.setDescription(request.getDescription());
        brand.setLogoUrl(request.getLogoUrl());

        return brandMapper.toDto(brandDao.save(brand));
    }

    @Override
    public void deleteBrand(Long id) {
        if (!brandDao.existsById(id)) {
            throw new NotFoundException("Brand not found");
        }
        brandDao.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BrandResponse getBrandById(Long id) {
        return brandMapper.toDto(brandDao.findById(id)
                .orElseThrow(() -> new NotFoundException("Brand not found")));
    }

    @Override
    @Transactional(readOnly = true)
    public List<BrandResponse> getAllBrands() {
        return brandDao.findAllByOrderByNameAsc().stream()
                .map(brandMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BrandResponse> searchBrands(String name) {
        return brandDao.findByNameContaining(name).stream()
                .map(brandMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BrandResponse> getBrandsWithPerfumes() {
        return brandDao.findBrandsWithPerfumes().stream()
                .map(brandMapper::toDto)
                .collect(Collectors.toList());
    }
}