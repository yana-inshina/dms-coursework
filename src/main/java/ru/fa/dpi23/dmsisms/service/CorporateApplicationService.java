// ru.fa.dpi23.dmsisms.service.CorporateApplicationService.java
package ru.fa.dpi23.dmsisms.service;

import ru.fa.dpi23.dmsisms.entity.CorporateApplication;
import ru.fa.dpi23.dmsisms.entity.CorporateClient;

import java.util.List;

public interface CorporateApplicationService {

    List<CorporateApplication> findAll();

    List<CorporateApplication> findByClient(CorporateClient client);

    CorporateApplication findById(Long id);

    /**
     * Создание/обновление заявки с расчётом премии (FR-09).
     */
    CorporateApplication createOrUpdate(CorporateApplication app);

    void approve(Long id, String managerComment);

    void reject(Long id, String managerComment);

    void convertToPolicy(Long id);
}
