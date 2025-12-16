package ru.fa.dpi23.dmsisms.config;

import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import ru.fa.dpi23.dmsisms.entity.Coverage;
import ru.fa.dpi23.dmsisms.repository.CoverageRepository;

/**
 * Конвертер из строки (ID) в сущность Coverage для биндинга форм.
 */
@Component
@RequiredArgsConstructor
public class StringToCoverageConverter implements Converter<String, Coverage> {

    private final CoverageRepository coverageRepository;

    @Override
    public Coverage convert(String source) {
        if (source == null || source.isBlank()) return null;
        Long id = Long.valueOf(source);
        return coverageRepository.findById(id).orElse(null);
    }
}