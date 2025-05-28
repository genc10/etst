package org.perfume.service;

import org.perfume.model.dto.request.PerfumeRequest;
import org.perfume.model.dto.request.ProductFilterRequest;
import org.perfume.model.dto.response.PageResponse;
import org.perfume.model.dto.response.PerfumeResponse;

import java.util.List;

public interface PerfumeService {
    PerfumeResponse createPerfume(PerfumeRequest request);
    PerfumeResponse updatePerfume(Long id, PerfumeRequest request);
    void deletePerfume(Long id);
    PerfumeResponse getPerfumeById(Long id);
    PageResponse<PerfumeResponse> getAllPerfumes(ProductFilterRequest filterRequest);
    List<PerfumeResponse> getFeaturedPerfumes();
    List<PerfumeResponse> getBestsellerPerfumes();
    List<PerfumeResponse> getDiscountedPerfumes();
    List<PerfumeResponse> getLatestPerfumes();
    List<PerfumeResponse> getSimilarPerfumes(Long perfumeId);
    List<PerfumeResponse> searchPerfumes(String query);
}