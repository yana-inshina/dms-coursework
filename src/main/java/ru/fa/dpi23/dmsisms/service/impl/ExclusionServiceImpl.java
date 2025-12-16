package ru.fa.dpi23.dmsisms.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.fa.dpi23.dmsisms.entity.Exclusion;
import ru.fa.dpi23.dmsisms.repository.ExclusionRepository;
import ru.fa.dpi23.dmsisms.service.ExclusionService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExclusionServiceImpl implements ExclusionService {

    private final ExclusionRepository exclusionRepository;

    @Override
    public List<Exclusion> findAll(String keyword, Sort sort) {
        if (keyword != null && !keyword.isBlank()) {
            return exclusionRepository.findByNameContainingIgnoreCase(keyword.trim(), sort);
        }
        return exclusionRepository.findAll(sort);
    }

    @Override
    public Exclusion findById(Long id) {
        return exclusionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Исключение не найдено: " + id));
    }

    @Override
    public Exclusion save(Exclusion exclusion) {
        return exclusionRepository.save(exclusion);
    }

    @Override
    public void deleteById(Long id) {
        Exclusion exclusion = exclusionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Исключение не найдено: " + id));
        // Проверяем, что исключение не привязано к продуктам (FR-04)
        if (exclusion.getProducts() != null && !exclusion.getProducts().isEmpty()) {
            throw new IllegalStateException(
                    "Нельзя удалить исключение, так как оно используется в одном или нескольких продуктах.");
        }
        exclusionRepository.deleteById(id);
    }
}