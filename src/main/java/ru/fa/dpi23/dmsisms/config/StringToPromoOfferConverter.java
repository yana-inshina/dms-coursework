package ru.fa.dpi23.dmsisms.config;

import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import ru.fa.dpi23.dmsisms.entity.PromoOffer;
import ru.fa.dpi23.dmsisms.repository.PromoOfferRepository;

/**
 * Конвертер из строки (ID) в сущность PromoOffer для биндинга форм.
 */
@Component
@RequiredArgsConstructor
public class StringToPromoOfferConverter implements Converter<String, PromoOffer> {

    private final PromoOfferRepository promoOfferRepository;

    @Override
    public PromoOffer convert(String source) {
        if (source == null || source.isBlank()) return null;
        Long id = Long.valueOf(source);
        return promoOfferRepository.findById(id).orElse(null);
    }
}