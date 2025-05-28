package org.perfume.service;

import org.perfume.model.dto.request.BrandRequest;
import org.perfume.model.dto.response.BrandResponse;

import java.util.List;

public interface BrandService {
    List<BrandResponse> searchBrands(String name);
    List<BrandResponse> getBrandsWithPerfumes();
    BrandResponse create(BrandRequest request);
    BrandResponse update(Long id, BrandRequest request);
    void delete(Long id);
    BrandResponse getById(Long id);
    List<BrandResponse> getAll();
}