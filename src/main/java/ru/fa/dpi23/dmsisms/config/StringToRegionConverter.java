package ru.fa.dpi23.dmsisms.config;

import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import ru.fa.dpi23.dmsisms.entity.Region;
import ru.fa.dpi23.dmsisms.repository.RegionRepository;

/**
 * Конвертер из строки (ID) в сущность Region для биндинга форм.
 */
@Component
@RequiredArgsConstructor
public class StringToRegionConverter implements Converter<String, Region> {

    private final RegionRepository regionRepository;

    @Override
    public Region convert(String source) {
        if (source == null || source.isBlank()) return null;
        Long id = Long.valueOf(source);
        return regionRepository.findById(id).orElse(null);
    }
}