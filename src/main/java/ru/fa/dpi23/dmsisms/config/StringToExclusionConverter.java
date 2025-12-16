package ru.fa.dpi23.dmsisms.config;

import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import ru.fa.dpi23.dmsisms.entity.Exclusion;
import ru.fa.dpi23.dmsisms.repository.ExclusionRepository;

/**
 * Конвертер из строки (ID) в сущность Exclusion для биндинга форм.
 */
@Component
@RequiredArgsConstructor
public class StringToExclusionConverter implements Converter<String, Exclusion> {

    private final ExclusionRepository exclusionRepository;

    @Override
    public Exclusion convert(String source) {
        if (source == null || source.isBlank()) return null;
        Long id = Long.valueOf(source);
        return exclusionRepository.findById(id).orElse(null);
    }
}