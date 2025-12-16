package ru.fa.dpi23.dmsisms.service;

import org.springframework.data.domain.Sort;
import ru.fa.dpi23.dmsisms.entity.PromoOffer;

import java.util.List;

public interface PromoOfferService {

    List<PromoOffer> findAll(String keyword, Sort sort);

    PromoOffer findById(Long id);

    PromoOffer save(PromoOffer promoOffer);

    void deleteById(Long id);

    boolean canDeletePromoOffer(Long promoOfferId);
}
