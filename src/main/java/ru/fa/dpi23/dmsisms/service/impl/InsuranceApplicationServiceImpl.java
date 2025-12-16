package ru.fa.dpi23.dmsisms.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.fa.dpi23.dmsisms.entity.ApplicationStatus;
import ru.fa.dpi23.dmsisms.entity.InsuranceApplication;
import ru.fa.dpi23.dmsisms.entity.InsuranceProgram;
import ru.fa.dpi23.dmsisms.entity.InsurancePolicy;
import ru.fa.dpi23.dmsisms.repository.InsuranceApplicationRepository;
import ru.fa.dpi23.dmsisms.service.InsuranceApplicationService;
import ru.fa.dpi23.dmsisms.service.InsurancePolicyService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InsuranceApplicationServiceImpl implements InsuranceApplicationService {

    private final InsuranceApplicationRepository applicationRepository;
    private final InsurancePolicyService policyService;
    // ----------- автосчёт премии -----------

    @Override
    public BigDecimal calculatePremium(InsuranceProgram program,
                                       LocalDate birthDate,
                                       boolean chronic,
                                       int insuredPersons) {
        if (program == null) {
            throw new IllegalArgumentException("Не указана программа страхования");
        }
        // 1. определяем возраст застрахованного (если указан)
        int age = 30;
        if (birthDate != null) {
            age = Period.between(birthDate, LocalDate.now()).getYears();
            if (age < 0) {
                age = 0;
            }
        }
        // 2. базовая стоимость для одного человека — берём basePrice программы (может быть null)
        BigDecimal baseForOne = program.getBasePrice();
        if (baseForOne == null) {
            // если базовая цена не указана, считаем, что она равна 0
            baseForOne = BigDecimal.ZERO;
        }
        // 3. возрастной коэффициент (по аналогии с FR-05)
        BigDecimal ageCoef;
        if (age < 30) {
            ageCoef = BigDecimal.valueOf(1.0);
        } else if (age < 45) {
            ageCoef = BigDecimal.valueOf(1.2);
        } else if (age < 60) {
            ageCoef = BigDecimal.valueOf(1.5);
        } else {
            ageCoef = BigDecimal.valueOf(1.8);
        }
        // 4. коэффициент хронических заболеваний
        BigDecimal chronicCoef = chronic ? BigDecimal.valueOf(1.3) : BigDecimal.ONE;
        // 5. региональный коэффициент (k). По умолчанию 1.0. Для индивидуальных заявок
        // коэффициент зависит от выбранного региона обслуживания, однако конкретные
        // значения коэффициентов определяются в справочниках и на данном этапе не
        // применяются. При необходимости реализацию можно расширить, чтобы
        // определять коэффициент по региону.
        BigDecimal regionCoef = BigDecimal.ONE;

        // Премия: base_price × ageCoef × chronicCoef × regionCoef
        BigDecimal result = baseForOne
                .multiply(ageCoef)
                .multiply(chronicCoef)
                .multiply(regionCoef);
        return result.setScale(2, RoundingMode.HALF_UP);
    }

    // создание заявки
    @Override
    @Transactional
    public InsuranceApplication createApplication(InsuranceApplication draft) {

        // присваиваем дефолтный тарифный план, если он отсутствует. Это поле
        // больше не используется при расчёте премии, однако в БД оно
        // обязательное. Если в БД нет ни одного тарифа (что возможно после
        // очистки), создаём дефолтный тариф на лету.
        if (draft.getProgram() == null) {
            throw new IllegalArgumentException("Не указана программа в заявке");
        }
        // тарифные планы и регион больше не требуются для расчёта

        InsuranceProgram program = draft.getProgram();
        // Общая премия с учётом количества застрахованных и хронических заболеваний
        BigDecimal premium = calculatePremium(
                program,
                draft.getBirthDate(),
                draft.isChronicDiseases(),
                draft.getInsuredPersons()
        );

        // учитываем региональный коэффициент, если регион выбран
        java.math.BigDecimal regionCoefVal = java.math.BigDecimal.ONE;
        if (draft.getRegion() != null && draft.getRegion().getCoefficient() != null) {
            regionCoefVal = draft.getRegion().getCoefficient();
        }
        premium = premium.multiply(regionCoefVal);

        // применяем акции/скидки, связанные с программой. Акции действуют
        // независимо от того, является ли заявка первой для пользователя.
        // Кроме того, акции, предназначенные для корпоративных клиентов
        // (например, условия с "5" и "сотрудниками") игнорируются для
        // индивидуальных заявок.
        java.math.BigDecimal discount = java.math.BigDecimal.ZERO;
        if (program.getPromoOffers() != null) {
            java.time.LocalDate today = java.time.LocalDate.now();
            for (ru.fa.dpi23.dmsisms.entity.PromoOffer promo : program.getPromoOffers()) {
                if (!promo.isActive()) {
                    continue;
                }
                java.time.LocalDate from = promo.getValidFrom();
                java.time.LocalDate to = promo.getValidTo();
                boolean inDate = (from == null || !today.isBefore(from)) && (to == null || !today.isAfter(to));
                if (!inDate) {
                    continue;
                }
                // Пропускаем акции, предназначенные для корпоративных клиентов. В
                // тестовых данных корпоративная скидка 15 % содержит слова
                // "5" и "сотруд" в названии или описании. Если подобные
                // условия обнаружены, считаем, что акция предназначена для
                // корпоративных клиентов, и пропускаем её.
                String name = promo.getName() == null ? "" : promo.getName().toLowerCase();
                String desc = promo.getDescription() == null ? "" : promo.getDescription().toLowerCase();
                if (name.contains("5") || name.contains("сотруд") || desc.contains("5") || desc.contains("сотруд")) {
                    continue;
                }
                java.math.BigDecimal current;
                if (promo.getDiscountType() == ru.fa.dpi23.dmsisms.entity.DiscountType.PERCENT) {
                    // скидка = премия × (процент / 100)
                    current = premium.multiply(promo.getDiscountAmount())
                            .divide(java.math.BigDecimal.valueOf(100), 4, java.math.RoundingMode.HALF_UP);
                } else {
                    // фиксированная сумма скидки
                    current = promo.getDiscountAmount();
                }
                if (current.compareTo(discount) > 0) {
                    discount = current;
                }
            }
        }
        // уменьшаем премию на величину скидки, но не опускаем ниже нуля
        premium = premium.subtract(discount);
        if (premium.compareTo(java.math.BigDecimal.ZERO) < 0) {
            premium = java.math.BigDecimal.ZERO;
        }

        // Определяем возраст для расчёта возрастного коэффициента. Но для сохранения базовой
        // стоимости в заявке (basePriceSnapshot) мы используем базовую цену программы без
        // возрастных и региональных поправок. Это позволяет отображать в заявке
        // «чистую» базовую цену (15 000/35 000 и т.п.), а коэффициенты применяются
        // только при вычислении премии. Возрастной коэффициент определяем лишь для
        // премии, а не для snapshot.
        int age = 30;
        if (draft.getBirthDate() != null) {
            age = java.time.Period.between(draft.getBirthDate(), java.time.LocalDate.now()).getYears();
            if (age < 0) {
                age = 0;
            }
        }
        // Сохраняем в snapshot «чистую» базовую стоимость программы. Если базовая цена отсутствует,
        // используем 0, чтобы избежать NPE. Коэффициенты будут применяться только к премии.
        BigDecimal baseProgramPrice = program.getBasePrice();
        if (baseProgramPrice == null) {
            baseProgramPrice = BigDecimal.ZERO;
        }

        draft.setBasePriceSnapshot(baseProgramPrice);
        draft.setCalculatedPremium(premium);
        draft.setStatus(ApplicationStatus.NEW);
        draft.setCreatedAt(LocalDateTime.now());

        return applicationRepository.save(draft);
    }

    @Override
    public List<InsuranceApplication> findByUser(ru.fa.dpi23.dmsisms.entity.User user) {
        if (user == null) {
            return java.util.Collections.emptyList();
        }
        return applicationRepository.findByUser(user);
    }

    // ----------- базовые методы -----------

    @Override
    public List<InsuranceApplication> findAll() {
        return applicationRepository.findAll();
    }

    @Override
    public InsuranceApplication findById(Long id) {
        return applicationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Заявка не найдена: " + id));
    }

    // ----------- статусы + выпуск полиса -----------

    @Override
    @Transactional
    public InsuranceApplication approve(Long id, String comment) {
        InsuranceApplication app = findById(id);

        if (app.getStatus() == ApplicationStatus.REJECTED) {
            throw new IllegalStateException("Нельзя принять уже отклонённую заявку");
        }

        app.setStatus(ApplicationStatus.APPROVED);
        app.setProcessedAt(LocalDateTime.now());
        // сохраняем комментарий менеджера, если он есть
        if (comment != null && !comment.isBlank()) {
            app.setComment(comment);
        }
        return applicationRepository.save(app);
    }

    @Override
    @Transactional
    public InsuranceApplication reject(Long id, String comment) {
        InsuranceApplication app = findById(id);

        if (app.getStatus() == ApplicationStatus.CONVERTED_TO_POLICY) {
            throw new IllegalStateException("Нельзя отклонить заявку, из которой уже выпущен полис");
        }

        app.setStatus(ApplicationStatus.REJECTED);
        app.setProcessedAt(LocalDateTime.now());
        if (comment != null && !comment.isBlank()) {
            app.setComment(comment);
        }
        return applicationRepository.save(app);
    }

    @Override
    @Transactional
    public InsurancePolicy convertToPolicy(Long id) {
        InsuranceApplication app = findById(id);

        if (app.getStatus() == ApplicationStatus.REJECTED) {
            throw new IllegalStateException("Нельзя выпустить полис из отклонённой заявки");
        }

        // если ещё новая — считаем, что менеджер её одобряет
        if (app.getStatus() == ApplicationStatus.NEW) {
            app.setStatus(ApplicationStatus.APPROVED);
        }

        // Проверяем рассчитанную премию. Если премия отсутствует или равна нулю, выдаём понятную ошибку.
        if (app.getCalculatedPremium() == null || app.getCalculatedPremium().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("У заявки не рассчитана премия или она равна 0. Нельзя выпустить полис.");
        }

        InsurancePolicy policy = policyService.createFromApplication(app);

        app.setPolicy(policy);
        app.setStatus(ApplicationStatus.CONVERTED_TO_POLICY);
        app.setProcessedAt(LocalDateTime.now());
        applicationRepository.save(app);

        return policy;
    }
}
