package ru.fa.dpi23.dmsisms.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.fa.dpi23.dmsisms.entity.PromoOffer;

import java.util.List;

public interface PromoOfferRepository extends JpaRepository<PromoOffer, Long> {

    List<PromoOffer> findByNameContainingIgnoreCase(String name, Sort sort);
}
