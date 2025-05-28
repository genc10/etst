package org.perfume.service;

import org.perfume.model.dto.request.BrandRequest;
import org.perfume.model.dto.response.BrandResponse;

import java.util.List;

public interface BrandService {
    List<BrandResponse> searchBrands(String name);
    List<BrandResponse> getBrandsWithPerfumes();
}