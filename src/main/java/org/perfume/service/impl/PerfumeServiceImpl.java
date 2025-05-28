package org.perfume.service.impl;

import lombok.RequiredArgsConstructor;
import org.perfume.domain.entity.Perfume;
import org.perfume.domain.repo.PerfumeDao;
import org.perfume.exception.AlreadyExistsException;
import org.perfume.exception.NotFoundException;
import org.perfume.mapper.PerfumeMapper;
import org.perfume.model.dto.request.PerfumeRequest;
import org.perfume.model.dto.request.ProductFilterRequest;
import org.perfume.model.dto.response.PageResponse;
import org.perfume.model.dto.response.PerfumeResponse;
import org.perfume.service.PerfumeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PerfumeServiceImpl implements PerfumeService {

    private final PerfumeDao perfumeDao;
    private final PerfumeMapper perfumeMapper;

    @Override
    public PerfumeResponse createPerfume(PerfumeRequest request) {
        if (perfumeDao.existsByName(request.getName())) {
            throw new AlreadyExistsException("Perfume with name " + request.getName() + " already exists");
        }

        Perfume perfume = perfumeMapper.toEntity(request);
        return perfumeMapper.toDto(perfumeDao.save(perfume));
    }

    @Override
    public PerfumeResponse updatePerfume(Long id, PerfumeRequest request) {
        Perfume perfume = perfumeDao.findById(id)
                .orElseThrow(() -> new NotFoundException("Perfume not found"));

        if (!perfume.getName().equals(request.getName()) && perfumeDao.existsByName(request.getName())) {
            throw new AlreadyExistsException("Perfume with name " + request.getName() + " already exists");
        }

        Perfume updatedPerfume = perfumeMapper.toEntity(request);
        updatedPerfume.setId(id);
        return perfumeMapper.toDto(perfumeDao.save(updatedPerfume));
    }

    @Override
    public void deletePerfume(Long id) {
        if (!perfumeDao.existsById(id)) {
            throw new NotFoundException("Perfume not found");
        }
        perfumeDao.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public PerfumeResponse getPerfumeById(Long id) {
        return perfumeMapper.toDto(perfumeDao.findById(id)
                .orElseThrow(() -> new NotFoundException("Perfume not found")));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PerfumeResponse> getAllPerfumes(ProductFilterRequest filterRequest) {
        Sort sort = Sort.by(Sort.Direction.fromString(filterRequest.getSortDirection()), filterRequest.getSortBy());
        PageRequest pageRequest = PageRequest.of(filterRequest.getPage(), filterRequest.getSize(), sort);

        Page<Perfume> perfumePage = perfumeDao.findWithFilters(
                filterRequest.getSearch(),
                filterRequest.getBrandId(),
                filterRequest.getCategoryId(),
                filterRequest.getMinPrice(),
                filterRequest.getMaxPrice(),
                filterRequest.getGender(),
                filterRequest.getFragranceFamily(),
                filterRequest.getFeatured(),
                filterRequest.getBestseller(),
                pageRequest
        );

        List<PerfumeResponse> content = perfumePage.getContent().stream()
                .map(perfumeMapper::toDto)
                .collect(Collectors.toList());

        return new PageResponse<>(
                content,
                perfumePage.getNumber(),
                perfumePage.getSize(),
                perfumePage.getTotalElements(),
                perfumePage.getTotalPages(),
                perfumePage.isLast(),
                perfumePage.isFirst()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<PerfumeResponse> getFeaturedPerfumes() {
        return perfumeDao.findByFeaturedTrue().stream()
                .map(perfumeMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PerfumeResponse> getBestsellerPerfumes() {
        return perfumeDao.findByBestsellerTrue().stream()
                .map(perfumeMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PerfumeResponse> getDiscountedPerfumes() {
        return perfumeDao.findDiscountedPerfumes().stream()
                .map(perfumeMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PerfumeResponse> getLatestPerfumes() {
        return perfumeDao.findLatestPerfumes(PageRequest.of(0, 10)).stream()
                .map(perfumeMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PerfumeResponse> getSimilarPerfumes(Long perfumeId) {
        Perfume perfume = perfumeDao.findById(perfumeId)
                .orElseThrow(() -> new NotFoundException("Perfume not found"));

        return perfumeDao.findSimilarPerfumes(
                        perfume.getBrand().getId(),
                        perfume.getCategory().getId(),
                        perfumeId
                ).stream()
                .map(perfumeMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PerfumeResponse> searchPerfumes(String query) {
        return perfumeDao.findByNameContainingIgnoreCase(query).stream()
                .map(perfumeMapper::toDto)
                .collect(Collectors.toList());
    }
}