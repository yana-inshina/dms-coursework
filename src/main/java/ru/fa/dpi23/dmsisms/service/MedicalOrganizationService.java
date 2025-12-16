package ru.fa.dpi23.dmsisms.service;

import org.springframework.data.domain.Sort;
import ru.fa.dpi23.dmsisms.entity.MedicalOrganization;

import java.util.List;

public interface MedicalOrganizationService {

    List<MedicalOrganization> findAll(String keyword, Sort sort);

    MedicalOrganization findById(Long id);

    MedicalOrganization save(MedicalOrganization org);

    void deleteById(Long id);
}