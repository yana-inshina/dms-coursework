package ru.fa.dpi23.dmsisms.config;

import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import ru.fa.dpi23.dmsisms.entity.MedicalOrganization;
import ru.fa.dpi23.dmsisms.repository.MedicalOrganizationRepository;

/**
 * Конвертер из строки (ID) в сущность MedicalOrganization для биндинга форм.
 */
@Component
@RequiredArgsConstructor
public class StringToMedicalOrganizationConverter implements Converter<String, MedicalOrganization> {

    private final MedicalOrganizationRepository medicalOrganizationRepository;

    @Override
    public MedicalOrganization convert(String source) {
        if (source == null || source.isBlank()) return null;
        Long id = Long.valueOf(source);
        return medicalOrganizationRepository.findById(id).orElse(null);
    }
}