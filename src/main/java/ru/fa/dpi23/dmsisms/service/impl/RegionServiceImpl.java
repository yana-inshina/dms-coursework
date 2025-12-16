package ru.fa.dpi23.dmsisms.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.fa.dpi23.dmsisms.entity.Region;
import ru.fa.dpi23.dmsisms.repository.RegionRepository;
import ru.fa.dpi23.dmsisms.service.RegionService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RegionServiceImpl implements RegionService {

    private final RegionRepository regionRepository;

    @Override
    public List<Region> findAll(String keyword, Sort sort) {
        if (keyword != null && !keyword.isBlank()) {
            return regionRepository.findByNameContainingIgnoreCase(keyword.trim(), sort);
        }
        return regionRepository.findAll(sort);
    }

    /**
     * Возвращает все регионы в алфавитном порядке. Реализуем этот метод здесь,
     * чтобы обойти проблему с использованием default‑метода в проксах Spring.
     * В некоторых версиях Spring JDK‑прокси не делегируют вызовы default
     * методов интерфейса, из‑за чего RegionService#findAllSortedByName() может
     * вернуть null или привести к ошибке. Явная реализация устраняет эту
     * проблему и упрощает понимание кода.
     */
    @Override
    public List<Region> findAllSortedByName() {
        return regionRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
    }

    @Override
    public Region findById(Long id) {
        return regionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Регион не найден: " + id));
    }

    @Override
    public Region save(Region region) {
        return regionRepository.save(region);
    }

    @Override
    public void deleteById(Long id) {
        regionRepository.deleteById(id);
    }
}