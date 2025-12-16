package ru.fa.dpi23.dmsisms.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.fa.dpi23.dmsisms.entity.PromoOffer;
import ru.fa.dpi23.dmsisms.repository.PromoOfferRepository;
import ru.fa.dpi23.dmsisms.service.PromoOfferService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PromoOfferServiceImpl implements PromoOfferService {

    private final PromoOfferRepository promoOfferRepository;

    @Override
    public List<PromoOffer> findAll(String keyword, Sort sort) {
        if (keyword != null && !keyword.isBlank()) {
            return promoOfferRepository.findByNameContainingIgnoreCase(keyword.trim(), sort);
        }
        return promoOfferRepository.findAll(sort);
    }

    @Override
    public PromoOffer findById(Long id) {
        return promoOfferRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Акция не найдена: " + id));
    }

    @Override
    public PromoOffer save(PromoOffer offer) {
        return promoOfferRepository.save(offer);
    }

    @Override
    public void deleteById(Long id) {
        promoOfferRepository.deleteById(id);
    }

    @Override
    public boolean canDeletePromoOffer(Long promoOfferId) {
        PromoOffer promoOffer = promoOfferRepository.findById(promoOfferId)
                .orElseThrow(() -> new IllegalArgumentException("Акция не найдена: " + promoOfferId));
        // Нельзя удалять акцию, если она связана с программами, тарифами или продуктами
        return promoOffer.getApplicableToPrograms().isEmpty()
                && promoOffer.getApplicableToProducts().isEmpty();
    }
}
