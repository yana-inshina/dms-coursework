package ru.fa.dpi23.dmsisms.config;

import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import ru.fa.dpi23.dmsisms.entity.CorporateClient;
import ru.fa.dpi23.dmsisms.repository.CorporateClientRepository;

/**
 * Converter that allows Spring MVC to convert a String representing the
 * identifier of a {@link CorporateClient} into the corresponding entity
 * instance. Without this converter, Thymeleaf forms that bind directly to
 * a CorporateClient property (e.g. {@code th:field="*{corporateClient}"})
 * would result in the property being left null, causing a
 * NullPointerException when the controller attempts to access
 * {@code corporateClient.getId()}. This converter looks up the
 * {@link CorporateClient} by id using the {@link CorporateClientRepository}.
 */
@Component
@RequiredArgsConstructor
public class StringToCorporateClientConverter implements Converter<String, CorporateClient> {

    private final CorporateClientRepository corporateClientRepository;

    @Override
    public CorporateClient convert(String source) {
        if (source == null || source.isBlank()) {
            return null;
        }
        try {
            Long id = Long.valueOf(source);
            return corporateClientRepository.findById(id).orElse(null);
        } catch (NumberFormatException ex) {
            // If the string cannot be parsed to a long, return null to avoid errors.
            return null;
        }
    }
}