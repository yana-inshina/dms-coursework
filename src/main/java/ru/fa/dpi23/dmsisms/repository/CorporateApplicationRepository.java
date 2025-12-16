package ru.fa.dpi23.dmsisms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.fa.dpi23.dmsisms.entity.ApplicationStatus;
import ru.fa.dpi23.dmsisms.entity.CorporateApplication;
import ru.fa.dpi23.dmsisms.entity.CorporateClient;

import java.util.List;

public interface CorporateApplicationRepository
        extends JpaRepository<CorporateApplication, Long> {

    List<CorporateApplication> findByCorporateClient(CorporateClient client);

    List<CorporateApplication> findByStatus(ApplicationStatus status);
}
