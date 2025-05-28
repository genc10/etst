package org.perfume.service;

import org.perfume.domain.entity.Brand;
import org.perfume.model.dto.request.BrandRequest;
import org.perfume.model.dto.response.BrandResponse;

import java.util.List;

public interface BrandService {
    BrandResponse create(BrandRequest request);

    void delete(Long id);

    BrandResponse update(Long id, BrandRequest request);

    BrandResponse getById(Long id);

    List<BrandResponse> getAll();

    List<BrandResponse> searchBrands(String name);

    List<BrandResponse> getBrandsWithPerfumes();
}
