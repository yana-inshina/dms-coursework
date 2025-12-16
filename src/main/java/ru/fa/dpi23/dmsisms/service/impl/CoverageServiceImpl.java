package ru.fa.dpi23.dmsisms.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.fa.dpi23.dmsisms.entity.Coverage;
import ru.fa.dpi23.dmsisms.repository.CoverageRepository;
import ru.fa.dpi23.dmsisms.service.CoverageService;

import java.util.List;

/**
 * Реализация сервиса справочника покрытий. Выполняет базовые CRUD‑операции и
 * проверяет привязку к продуктам перед удалением (FR-04).
 */
@Service
@RequiredArgsConstructor
public class CoverageServiceImpl implements CoverageService {

    private final CoverageRepository coverageRepository;

    @Override
    public List<Coverage> findAll(String keyword, Sort sort) {
        if (keyword != null && !keyword.isBlank()) {
            return coverageRepository.findByNameContainingIgnoreCase(keyword.trim(), sort);
        }
        return coverageRepository.findAll(sort);
    }

    @Override
    public Coverage findById(Long id) {
        return coverageRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Покрытие не найдено: " + id));
    }

    @Override
    public Coverage save(Coverage coverage) {
        return coverageRepository.save(coverage);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        Coverage coverage = coverageRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Покрытие не найдено: " + id));
        // Проверяем, что покрытие не привязано ни к одному продукту
        if (coverage.getProducts() != null && !coverage.getProducts().isEmpty()) {
            throw new IllegalStateException(
                    "Нельзя удалить покрытие, так как оно используется в одном или нескольких продуктах.");
        }
        coverageRepository.deleteById(id);
    }
}