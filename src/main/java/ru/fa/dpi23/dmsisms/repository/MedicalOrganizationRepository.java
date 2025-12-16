package ru.fa.dpi23.dmsisms.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.fa.dpi23.dmsisms.entity.MedicalOrganization;

import java.util.List;

public interface MedicalOrganizationRepository
        extends JpaRepository<MedicalOrganization, Long> {

    List<MedicalOrganization> findByNameContainingIgnoreCase(String name, Sort sort);
}
