package ru.fa.dpi23.dmsisms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.fa.dpi23.dmsisms.entity.CorporateClient;

import java.util.Optional;

public interface CorporateClientRepository extends JpaRepository<CorporateClient, Long> {

    Optional<CorporateClient> findByInn(String inn);

    // используем в DataInitializer
    Optional<CorporateClient> findFirstByInn(String inn);

    Optional<CorporateClient> findFirstByContactEmailOrderByIdAsc(String contactEmail);
}
