package ru.fa.dpi23.dmsisms.config;

import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import ru.fa.dpi23.dmsisms.entity.InsuranceProgram;
import ru.fa.dpi23.dmsisms.repository.InsuranceProgramRepository;

@Component
@RequiredArgsConstructor
public class StringToInsuranceProgramConverter implements Converter<String, InsuranceProgram> {

    private final InsuranceProgramRepository insuranceProgramRepository;

    @Override
    public InsuranceProgram convert(String source) {
        if (source == null || source.isBlank()) return null;

        Long id = Long.valueOf(source);
        return insuranceProgramRepository.findById(id).orElse(null);
    }
}
