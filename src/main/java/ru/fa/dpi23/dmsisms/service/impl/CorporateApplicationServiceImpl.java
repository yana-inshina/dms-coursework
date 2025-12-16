// ru.fa.dpi23.dmsisms.service.impl.CorporateApplicationServiceImpl.java
package ru.fa.dpi23.dmsisms.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.fa.dpi23.dmsisms.entity.ApplicationStatus;
import ru.fa.dpi23.dmsisms.entity.CorporateApplication;
import ru.fa.dpi23.dmsisms.entity.CorporateClient;
import ru.fa.dpi23.dmsisms.entity.InsuranceProgram;
import ru.fa.dpi23.dmsisms.entity.InsurancePolicy;
import ru.fa.dpi23.dmsisms.repository.CorporateApplicationRepository;
import ru.fa.dpi23.dmsisms.service.CorporateApplicationService;
import ru.fa.dpi23.dmsisms.service.InsurancePolicyService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CorporateApplicationServiceImpl implements CorporateApplicationService {

    private final CorporateApplicationRepository corporateApplicationRepository;
    private final InsurancePolicyService insurancePolicyService;
    // ================== ПУБЛИЧНЫЕ МЕТОДЫ ==================

    @Override
    @Transactional(readOnly = true)
    public List<CorporateApplication> findAll() {
        return corporateApplicationRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CorporateApplication> findByClient(CorporateClient client) {
        return corporateApplicationRepository.findByCorporateClient(client);
    }

    @Override
    @Transactional(readOnly = true)
    public CorporateApplication findById(Long id) {
        return corporateApplicationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Корпоративная заявка не найдена: " + id));
    }

    @Override
    public CorporateApplication createOrUpdate(CorporateApplication app) {

        // присваиваем дефолтный тарифный план, если он не выбран. Это необходимо
        // Проверяем обязательные поля
        if (app.getProgram() == null) {
            throw new IllegalArgumentException("Не указана программа ДМС");
        }
        // Расчёт премии производится на основе базовой цены программы.
        if (app.getServiceRegion() == null) {
            throw new IllegalArgumentException("Не выбран регион обслуживания");
        }
        if (app.getHeadcount() == null || app.getHeadcount() <= 0) {
            throw new IllegalArgumentException("Headcount должен быть больше 0");
        }

        // 1) считаем полную стоимость программы без учёта скидок: это
        // стоимость одного сотрудника (с возрастной поправкой) умноженная на количество
        BigDecimal totalPremiumBeforeDiscount = calculatePremium(
                app.getProgram(),
                app.getAverageAge(),
                app.getAgeBand(),
                app.getHeadcount() != null ? app.getHeadcount() : 1
        );
        // Если не удалось рассчитать премию, пытаемся ещё раз (результат может быть нулевым)
        if (totalPremiumBeforeDiscount == null || totalPremiumBeforeDiscount.compareTo(BigDecimal.ZERO) <= 0) {
            totalPremiumBeforeDiscount = calculatePremium(
                    app.getProgram(),
                    app.getAverageAge(),
                    app.getAgeBand(),
                    app.getHeadcount() != null ? app.getHeadcount() : 1
            );
        }

        // === Региональный коэффициент ===
        // Для корпоративных заявок учитываем региональный коэффициент, как и в индивидуальных заявках.
        // Коэффициент хранится в выбранном регионе обслуживания (serviceRegion) и влияет на общую премию.
        BigDecimal regionCoef = BigDecimal.ONE;
        if (app.getServiceRegion() != null && app.getServiceRegion().getCoefficient() != null) {
            regionCoef = app.getServiceRegion().getCoefficient();
        }
        totalPremiumBeforeDiscount = totalPremiumBeforeDiscount.multiply(regionCoef)
                .setScale(2, RoundingMode.HALF_UP);

        // 2) рассчитываем скидки (аналогично индивидуальной). Используем
        // totalPremiumBeforeDiscount для вычисления процента.
        BigDecimal discount = BigDecimal.ZERO;
        if (app.getProgram() != null && app.getProgram().getPromoOffers() != null) {
            java.time.LocalDate today = java.time.LocalDate.now();
            for (ru.fa.dpi23.dmsisms.entity.PromoOffer promo : app.getProgram().getPromoOffers()) {
                if (!promo.isActive()) continue;
                java.time.LocalDate from = promo.getValidFrom();
                java.time.LocalDate toDate = promo.getValidTo();
                boolean inDate = (from == null || !today.isBefore(from)) && (toDate == null || !today.isAfter(toDate));
                if (!inDate) continue;
                BigDecimal current;
                if (promo.getDiscountType() == ru.fa.dpi23.dmsisms.entity.DiscountType.PERCENT) {
                    current = totalPremiumBeforeDiscount.multiply(promo.getDiscountAmount())
                            .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
                } else {
                    current = promo.getDiscountAmount();
                }
                if (current.compareTo(discount) > 0) {
                    discount = current;
                }
            }
        }
        // корпоративная скидка 15% при headcount >=5
        if (app.getHeadcount() != null && app.getHeadcount() >= 5) {
            BigDecimal corporateDisc = totalPremiumBeforeDiscount
                    .multiply(BigDecimal.valueOf(15))
                    .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
            if (corporateDisc.compareTo(discount) > 0) {
                discount = corporateDisc;
            }
        }
        BigDecimal totalPremium = totalPremiumBeforeDiscount.subtract(discount);
        if (totalPremium.compareTo(BigDecimal.ZERO) < 0) {
            totalPremium = BigDecimal.ZERO;
        }

        // 3) базовая цена за 1 сотрудника (snapshot). По требованию заказчика
        // в заявке и полисе должна отображаться именно базовая стоимость программы,
        // без возрастного коэффициента и без учёта headcount. Поэтому в snapshot
        // сохраняем чистую basePrice программы. Если базовая цена отсутствует,
        // используем 0, чтобы избежать NPE. Коэффициенты применяются только
        // для расчёта премии.
        BigDecimal baseProgramPrice = app.getProgram() != null ? app.getProgram().getBasePrice() : null;
        if (baseProgramPrice == null) {
            baseProgramPrice = BigDecimal.ZERO;
        }

        app.setBasePriceSnapshot(baseProgramPrice);
        app.setCalculatedPremium(totalPremium);

        // для новых заявок ставим статус NEW
        if (app.getId() == null) {
            app.setStatus(ApplicationStatus.NEW);
            app.setCreatedAt(LocalDateTime.now());
        }

        return corporateApplicationRepository.save(app);
    }

    @Override
    public void approve(Long id, String managerComment) {
        CorporateApplication app = findById(id);
        app.setStatus(ApplicationStatus.APPROVED);
        app.setProcessedAt(LocalDateTime.now());
        app.setComment(managerComment);
        corporateApplicationRepository.save(app);
    }

    @Override
    public void reject(Long id, String managerComment) {
        CorporateApplication app = findById(id);
        app.setStatus(ApplicationStatus.REJECTED);
        app.setProcessedAt(LocalDateTime.now());
        app.setComment(managerComment);
        corporateApplicationRepository.save(app);
    }

    @Override
    public void convertToPolicy(Long id) {
        CorporateApplication app = findById(id);

        if (app.getStatus() == ApplicationStatus.REJECTED) {
            throw new IllegalStateException("Нельзя выпустить полис из отклонённой заявки");
        }

        // если заявка ещё новая — считаем, что менеджер её одобряет
        if (app.getStatus() == ApplicationStatus.NEW) {
            app.setStatus(ApplicationStatus.APPROVED);
        }

        // создаём групповой полис
        // Если премия не рассчитана или равна нулю, всё равно пытаемся выпустить полис. Проверка и корректировка
        // минимальной суммы будет выполнена в InsurancePolicyService.createCorporatePolicyFromApplication().
        InsurancePolicy policy = insurancePolicyService.createCorporatePolicyFromApplication(app);

        // связываем заявку и полис (если в CorporateApplication есть поле policy)
        app.setPolicy(policy);
        app.setStatus(ApplicationStatus.CONVERTED_TO_POLICY);
        app.setProcessedAt(LocalDateTime.now());

        corporateApplicationRepository.save(app);
    }

    // ================== ВНУТРЕННЯЯ ЛОГИКА FR-09 ==================

    /**
     * FR-09 / FR-05:
     * base_price (с учётом возраста) * headcount.
     */
    private BigDecimal calculatePremium(InsuranceProgram program,
                                        Integer averageAge,
                                        String ageBand,
                                        int headcount) {

        if (program == null || program.getBasePrice() == null) {
            throw new IllegalArgumentException("У программы должна быть задана базовая цена");
        }

        // 1) определяем «рабочий» возраст
        int age = 35; // дефолт

        if (averageAge != null) {
            age = averageAge;
        } else if (ageBand != null) {
            String band = ageBand.trim();
            if (band.startsWith("18")) {         // 18–29
                age = 25;
            } else if (band.startsWith("30")) {  // 30–44
                age = 37;
            } else if (band.startsWith("45")) {  // 45–59
                age = 50;
            } else if (band.startsWith("60")) {  // 60+
                age = 62;
            }
        }

        // 2) возрастной коэффициент — как в FR-05
        BigDecimal ageCoef;
        if (age < 30) {
            ageCoef = BigDecimal.valueOf(1.0);
        } else if (age < 45) {
            ageCoef = BigDecimal.valueOf(1.2);
        } else if (age < 60) {
            ageCoef = BigDecimal.valueOf(1.5);
        } else {
            ageCoef = BigDecimal.valueOf(2.0);
        }

        // 3) base_price для одного сотрудника (с возрастной поправкой)
        BigDecimal perEmployeeBase = program.getBasePrice()
                .multiply(ageCoef)
                .setScale(2, RoundingMode.HALF_UP);

        // 4) итог по headcount
        int cnt = Math.max(1, headcount);
        return perEmployeeBase
                .multiply(BigDecimal.valueOf(cnt))
                .setScale(2, RoundingMode.HALF_UP);
    }
}
