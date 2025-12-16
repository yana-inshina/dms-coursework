package ru.fa.dpi23.dmsisms.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.fa.dpi23.dmsisms.entity.MedicalOrganization;
import ru.fa.dpi23.dmsisms.repository.MedicalOrganizationRepository;
import ru.fa.dpi23.dmsisms.service.MedicalOrganizationService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MedicalOrganizationServiceImpl implements MedicalOrganizationService {

    private final MedicalOrganizationRepository medicalOrganizationRepository;

    @Override
    public List<MedicalOrganization> findAll(String keyword, Sort sort) {
        if (keyword != null && !keyword.isBlank()) {
            return medicalOrganizationRepository
                    .findByNameContainingIgnoreCase(keyword.trim(), sort);
        }
        return medicalOrganizationRepository.findAll(sort);
    }

    @Override
    public MedicalOrganization findById(Long id) {
        return medicalOrganizationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Медорганизация не найдена: " + id));
    }

    @Override
    public MedicalOrganization save(MedicalOrganization org) {
        // FR-07: нельзя сохранять медорганизацию без региона
        if (org.getRegion() == null) {
            throw new IllegalStateException("Необходимо указать регион для медорганизации.");
        }
        return medicalOrganizationRepository.save(org);
    }

    @Override
    public void deleteById(Long id) {
        MedicalOrganization org = medicalOrganizationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Медорганизация не найдена: " + id));
        // Проверяем, что нет привязки к продуктам (FR-07)
        if (org.getProducts() != null && !org.getProducts().isEmpty()) {
            throw new IllegalStateException(
                    "Нельзя удалить медорганизацию, так как она связана с одним или несколькими продуктами.");
        }
        medicalOrganizationRepository.deleteById(id);
    }
}