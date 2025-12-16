package ru.fa.dpi23.dmsisms.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import ru.fa.dpi23.dmsisms.entity.*;
import ru.fa.dpi23.dmsisms.repository.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;

    private final ClientRepository clientRepository;
    private final InsuranceProgramRepository insuranceProgramRepository;
    private final InsurancePolicyRepository insurancePolicyRepository;

    private final CoverageRepository coverageRepository;
    private final ExclusionRepository exclusionRepository;
    private final PromoOfferRepository promoOfferRepository;
    private final RegionRepository regionRepository;
    private final MedicalOrganizationRepository medicalOrganizationRepository;

    private final CorporateClientRepository corporateClientRepository;
    private final CorporateApplicationRepository corporateApplicationRepository;

    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {

        // ===== 1) РОЛИ =====
        Role adminRole = roleRepository.findByName(RoleName.ADMIN)
                .orElseGet(() -> roleRepository.save(new Role(RoleName.ADMIN)));

        Role managerRole = roleRepository.findByName(RoleName.MANAGER)
                .orElseGet(() -> roleRepository.save(new Role(RoleName.MANAGER)));

        roleRepository.findByName(RoleName.INDIVIDUAL)
                .orElseGet(() -> roleRepository.save(new Role(RoleName.INDIVIDUAL)));

        roleRepository.findByName(RoleName.CORPORATE)
                .orElseGet(() -> roleRepository.save(new Role(RoleName.CORPORATE)));

        // ===== 2) ПОЛЬЗОВАТЕЛИ (admin / manager) =====
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("adm"));
            admin.setFullName("Администратор системы");
            admin.setEmail("admin@example.com");
            admin.setEnabled(true);
            admin = userRepository.save(admin);

            UserRole ur = new UserRole();
            ur.setUser(admin);
            ur.setRole(adminRole);
            userRoleRepository.save(ur);
        }

        if (!userRepository.existsByUsername("manager")) {
            User manager = new User();
            manager.setUsername("manager");
            manager.setPassword(passwordEncoder.encode("man"));
            manager.setFullName("Менеджер");
            manager.setEmail("manager@example.com");
            manager.setEnabled(true);
            manager = userRepository.save(manager);

            UserRole ur = new UserRole();
            ur.setUser(manager);
            ur.setRole(managerRole);
            userRoleRepository.save(ur);
        }

        // ===== 3) КЛИЕНТЫ (физ / юр в общей таблице clients) =====

        // физлицо
        Client individual = clientRepository.findFirstByEmailOrderByIdAsc("ivanov@example.com")
                .orElseGet(() -> clientRepository.save(
                        Client.builder()
                                .fullName("Иванов Иван Иванович")
                                .email("ivanov@example.com")
                                .phone("+7-900-000-00-01")
                                .clientType("INDIVIDUAL")
                                .build()
                ));

        // юрлицо в общей таблице clients — для обычных полисов
        Client corporate = clientRepository.findFirstByEmailOrderByIdAsc("corp@example.com")
                .orElseGet(() -> clientRepository.save(
                        Client.builder()
                                .fullName("ООО «Здоровье Плюс»")
                                .email("corp@example.com")
                                .phone("+7-900-000-00-02")
                                .clientType("CORPORATE")
                                .build()
                ));

        // ===== 4) ПРОГРАММЫ (по code) =====
        InsuranceProgram basic = insuranceProgramRepository.findByCode("BASIC_2024")
                .orElseGet(() -> {
                    // Создаём базовую программу, если отсутствует. Устанавливаем также страховую сумму по умолчанию.
                    InsuranceProgram prog = InsuranceProgram.builder()
                            .code("BASIC_2024")
                            .name("Базовая программа ДМС")
                            .description("Амбулаторная помощь, базовый перечень анализов.")
                            .basePrice(new BigDecimal("15000.00"))
                            .insuranceSum(new BigDecimal("2000000.00"))
                            .active(true)
                            .build();
                    return insuranceProgramRepository.save(prog);
                });

        InsuranceProgram premium = insuranceProgramRepository.findByCode("PREMIUM_2024")
                .orElseGet(() -> {
                    // Создаём премиум программу, если отсутствует. Устанавливаем страховую сумму по умолчанию.
                    InsuranceProgram prog = InsuranceProgram.builder()
                            .code("PREMIUM_2024")
                            .name("Премиум ДМС")
                            .description("Расширенное покрытие, стационар, стоматология.")
                            .basePrice(new BigDecimal("35000.00"))
                            .insuranceSum(new BigDecimal("7000000.00"))
                            .active(true)
                            .build();
                    return insuranceProgramRepository.save(prog);
                });

        // Устанавливаем страховые суммы для существующих программ, если они ещё не заданы
        if (basic.getInsuranceSum() == null) {
            basic.setInsuranceSum(new BigDecimal("2000000.00"));
            insuranceProgramRepository.save(basic);
        }
        if (premium.getInsuranceSum() == null) {
            premium.setInsuranceSum(new BigDecimal("7000000.00"));
            insuranceProgramRepository.save(premium);
        }

        // ===== 5) ПОЛИСЫ (по policyNumber) =====
        if (!insurancePolicyRepository.existsByPolicyNumber("DMS-0001")) {
            InsurancePolicy p1 = new InsurancePolicy();
            p1.setPolicyNumber("DMS-0001");
            p1.setClient(individual);
            p1.setProgram(basic);
            p1.setStartDate(LocalDate.now().minusMonths(1));
            p1.setEndDate(LocalDate.now().plusMonths(11));
            p1.setPremium(new BigDecimal("15000.00"));
            p1.setStatus(PolicyStatus.ACTIVE);
            insurancePolicyRepository.save(p1);
        }

        if (!insurancePolicyRepository.existsByPolicyNumber("DMS-0002")) {
            InsurancePolicy p2 = new InsurancePolicy();
            p2.setPolicyNumber("DMS-0002");
            p2.setClient(corporate);
            p2.setProgram(premium);
            p2.setStartDate(LocalDate.now().minusYears(1));
            p2.setEndDate(LocalDate.now().minusDays(1));
            p2.setPremium(new BigDecimal("35000.00"));
            p2.setStatus(PolicyStatus.EXPIRED);
            insurancePolicyRepository.save(p2);
        }

        // ===== 6) СПРАВОЧНИКИ =====

        if (coverageRepository.count() == 0) {
            coverageRepository.save(Coverage.builder()
                    .name("Терапевт и базовые специалисты")
                    .description("Прием терапевта, кардиолога, ЛОР, невролога по направлению.")
                    .active(true)
                    .build());

            coverageRepository.save(Coverage.builder()
                    .name("Амбулаторная диагностика")
                    .description("Стандартный перечень лабораторных и инструментальных исследований.")
                    .active(true)
                    .build());
        }

        if (exclusionRepository.count() == 0) {
            exclusionRepository.save(Exclusion.builder()
                    .name("Хронические заболевания в стадии декомпенсации")
                    .description("Не покрываются плановые мероприятия при тяжелой декомпенсации.")
                    .active(true)
                    .build());

            exclusionRepository.save(Exclusion.builder()
                    .name("Косметология и пластические операции")
                    .description("Не входят в стандартные программы ДМС.")
                    .active(true)
                    .build());
        }

        // Тарифные планы удалены из проекта, инициализация не требуется.

        if (promoOfferRepository.count() == 0) {
            // Создаём демо‑акцию. Необходимо задать тип и величину скидки,
            // иначе поля discountType/discountAmount будут null, что приведёт
            // к ошибке сохранения из‑за ограничения NOT NULL. Указываем
            // процентную скидку 10%.
            PromoOffer demoOffer = PromoOffer.builder()
                    .name("Скидка 10% при оформлении до конца месяца")
                    .description("Применяется к базовой стоимости полиса при первичном оформлении.")
                    .discountType(DiscountType.PERCENT)
                    .discountAmount(new BigDecimal("10"))
                    .validFrom(LocalDate.now().minusDays(7))
                    .validTo(LocalDate.now().plusDays(30))
                    .active(true)
                    .build();
            demoOffer = promoOfferRepository.save(demoOffer);

            // Привязываем созданную акцию к базовой программе, если она существует. Это
            // гарантирует, что на странице программы в разделе «Акции и скидки»
            // отобразится созданная акция. При добавлении новых программ данную
            // логику можно расширить, чтобы назначать акции по иным правилам.
            InsuranceProgram baseProgram = insuranceProgramRepository.findByCode("BASIC_2024")
                    .orElse(null);
            if (baseProgram != null) {
                baseProgram.getPromoOffers().add(demoOffer);
                insuranceProgramRepository.save(baseProgram);
            }
        }

        if (regionRepository.count() == 0) {
            regionRepository.save(Region.builder()
                    .name("Москва и МО")
                    .description("Город Москва и Московская область.")
                    .active(true)
                    .build());

            // FR-02: меняем аббревиатуру ЛО на более понятное "область".
            // Если в БД нет регионов, сохраняем Санкт-Петербург и область.
            regionRepository.save(Region.builder()
                    .name("Санкт-Петербург и область")
                    .description("Город Санкт-Петербург и Ленинградская область.")
                    .active(true)
                    .build());
        }

        // ===== 6) Демо-корпоративный клиент и медорганизации =====

        // возьмём любой регион (например, первый)
        Region anyRegion = regionRepository.findAll()
                .stream().findFirst().orElse(null);

        // если клиентов ещё нет – создаём демо-организацию
        if (anyRegion != null && corporateClientRepository.count() == 0) {
            CorporateClient corpClientSeed = CorporateClient.builder()
                    .name("ООО «Здоровье Плюс»")
                    .inn("7701001001")
                    .region(anyRegion)
                    .contactPerson("Иванов Пётр Сергеевич")
                    .contactEmail("corp@example.com")
                    .contactPhone("+7-900-000-02-02")
                    .build();

            corporateClientRepository.save(corpClientSeed);
        }

        // достанем демо-клиента (если его уже создали раньше – просто найдём по ИНН)
        CorporateClient corpClient = corporateClientRepository.findFirstByInn("7701001001")
                .orElse(null);

        // медорганизации
        if (medicalOrganizationRepository.count() == 0) {
            // для каждой медорганизации необходимо указать регион и ОГРН,
            // чтобы удовлетворять требованиям FR‑07.
            // Находим регионы по точному имени (метод findByName отсутствует, поэтому фильтруем вручную)
            Region region1 = regionRepository.findAll().stream()
                    .filter(r -> "Москва и МО".equalsIgnoreCase(r.getName()))
                    .findFirst()
                    .orElse(anyRegion);
            Region region2 = regionRepository.findAll().stream()
                    .filter(r -> "Санкт-Петербург и ЛО".equalsIgnoreCase(r.getName()))
                    .findFirst()
                    .orElse(anyRegion);

            medicalOrganizationRepository.save(MedicalOrganization.builder()
                    .name("Клиника «Здоровье»")
                    .ogrn("1027700123456")
                    .address("Москва, ул. Примерная, д. 1")
                    .phone("+7 (495) 000-00-00")
                    .region(region1)
                    .description("Частная клиника, оказывающая широкий спектр медицинских услуг.")
                    .active(true)
                    .build());

            medicalOrganizationRepository.save(MedicalOrganization.builder()
                    .name("Медцентр «Северная столица»")
                    .ogrn("1047800123456")
                    .address("Санкт-Петербург, Невский пр-т, д. 10")
                    .phone("+7 (812) 000-00-00")
                    .region(region2)
                    .description("Медицинский центр в Санкт-Петербурге.")
                    .active(true)
                    .build());
        }

        // создаём дополнительную корпоративную организацию, если в таблице их меньше двух
        if (corporateClientRepository.count() < 2) {
            // Ищем регион "Москва и МО" вручную, так как в репозитории нет метода findByName
            Region corpRegion = regionRepository.findAll().stream()
                    .filter(r -> "Москва и МО".equalsIgnoreCase(r.getName()))
                    .findFirst().orElse(anyRegion);

            CorporateClient extraClient = CorporateClient.builder()
                    .name("ООО «Пример Медицинский»")
                    .inn("7702002002")
                    .region(corpRegion)
                    .contactPerson("Сидоров Сергей")
                    .contactEmail("example@corp.local")
                    .contactPhone("+7 (900) 123-45-67")
                    .build();
            corporateClientRepository.save(extraClient);
        }

        // ===== 7) Демо-корпоративные заявки =====
        if (corporateApplicationRepository.count() == 0
                && corpClient != null
                && anyRegion != null) {

            InsuranceProgram program = insuranceProgramRepository.findByCode("BASIC_2024").orElse(null);

            if (program != null) {

                BigDecimal basePrice = program.getBasePrice();

                // Заявка №1 — NEW, со средним возрастом
                int headcount1 = 25;
                corporateApplicationRepository.save(
                        CorporateApplication.builder()
                                .corporateClient(corpClient)
                                .program(program)
                                .serviceRegion(anyRegion)
                                .headcount(headcount1)
                                .averageAge(34)
                                .basePriceSnapshot(basePrice)
                                .calculatedPremium(
                                        basePrice.multiply(BigDecimal.valueOf(headcount1))
                                )
                                .status(ApplicationStatus.NEW)
                                .createdAt(LocalDateTime.now().minusDays(1))
                                .comment("Демо-заявка №1")
                                .build()
                );

                // Заявка №2 — APPROVED, с возрастным бендом
                int headcount2 = 40;
                corporateApplicationRepository.save(
                        CorporateApplication.builder()
                                .corporateClient(corpClient)
                                .program(program)
                                .serviceRegion(anyRegion)
                                .headcount(headcount2)
                                .ageBand("30–39 лет")
                                .basePriceSnapshot(basePrice)
                                .calculatedPremium(
                                        basePrice.multiply(BigDecimal.valueOf(headcount2))
                                )
                                .status(ApplicationStatus.APPROVED)
                                .createdAt(LocalDateTime.now().minusDays(3))
                                .processedAt(LocalDateTime.now().minusDays(2))
                                .comment("Демо-заявка №2 — одобрена")
                                .build()
                );
            }
        }

        // ===== 8) ТЕСТОВЫЙ CORPORATE-ПОЛЬЗОВАТЕЛЬ + КОРПКЛИЕНТ =====
        initCorporateTestUser();
    }

    // ===================== ДОП. МЕТОД =====================

    private void initCorporateTestUser() {

        // 1. Роль CORPORATE
        Role corporateRole = roleRepository.findByName(RoleName.CORPORATE)
                .orElseGet(() -> roleRepository.save(new Role(RoleName.CORPORATE)));

        // 2. Пользователь – ЛОГИН = E-mail
        User corpUser = userRepository.findByUsername("corp1@demo.local")
                .orElseGet(() -> {
                    User u = new User();
                    u.setUsername("corp1@demo.local");                 // логин
                    u.setPassword(passwordEncoder.encode("corp123"));  // пароль
                    u.setFullName("Тестовый корпоративный клиент");
                    u.setEmail("corp1@demo.local");
                    u.setEnabled(true);
                    return userRepository.save(u);
                });

        // 3. Связь user ↔ role
        if (!userRoleRepository.existsByUserAndRole(corpUser, corporateRole)) {
            UserRole ur = new UserRole();
            ur.setUser(corpUser);
            ur.setRole(corporateRole);
            userRoleRepository.save(ur);
        }

        // 4. Корпоративный клиент для фильтрации заявок
        corporateClientRepository
                .findFirstByContactEmailOrderByIdAsc("corp1@demo.local")
                .orElseGet(() -> {
                    Region region = regionRepository.findAll()
                            .stream().findFirst().orElse(null);

                    CorporateClient c = new CorporateClient();
                    c.setName("ООО «КорпКлиент Тестовый»");
                    c.setInn("7700000000");
                    c.setContactPerson("Иванов Иван");
                    c.setContactEmail("corp1@demo.local");
                    c.setContactPhone("+7 (900) 000-00-00");
                    c.setRegion(region);

                    return corporateClientRepository.save(c);
                });
    }

}
