package org.perfume.service;

import org.perfume.model.dto.request.BrandRequest;
import org.perfume.model.dto.response.BrandResponse;

import java.util.List;

public interface BrandService {
    BrandResponse createBrand(BrandRequest request);
    BrandResponse updateBrand(Long id, BrandRequest request);
    void deleteBrand(Long id);
    BrandResponse getBrandById(Long id);
    List<BrandResponse> getAllBrands();
    List<BrandResponse> searchBrands(String name);
    List<BrandResponse> getBrandsWithPerfumes();
}