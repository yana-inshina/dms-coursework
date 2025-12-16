package ru.fa.dpi23.dmsisms.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Сущность страховой программы. Содержит базовую информацию о программе, набор покрытий,
 * исключений, связанных акций, доступных регионов и медицинских организаций. Тарифные планы
 * исключены из модели, поскольку тарифная система была удалена из проекта.
 */
@Entity
@Table(name = "insurance_programs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InsuranceProgram {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Внутренний код программы, например "BASIC_2025". Уникален в рамках таблицы.
     */
    @Column(nullable = false, unique = true, length = 50)
    private String code;

    /**
     * Название программы.
     */
    @Column(nullable = false, length = 200)
    private String name;

    /**
     * Краткое описание программы и предоставляемого покрытия.
     */
    @Column(length = 2000)
    private String description;

    /**
     * Базовая стоимость программы. Может быть null, если стоимость не определена.
     */
    @Column(precision = 12, scale = 2)
    private BigDecimal basePrice;

    /**
     * Страховая сумма на одного застрахованного. Для базовой программы по умолчанию 2 000 000,
     * для премиум – 7 000 000. Может быть null.
     */
    @Column(name = "insurance_sum", precision = 15, scale = 2)
    private BigDecimal insuranceSum;

    /**
     * Флаг активности. Неактивные программы скрываются из интерфейса.
     */
    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;

    /**
     * Набор покрытий, относящихся к этой программе. Используем таблицу program_coverages для связи.
     */
    @ManyToMany
    @JoinTable(
            name = "program_coverages",
            joinColumns = @JoinColumn(name = "program_id"),
            inverseJoinColumns = @JoinColumn(name = "coverage_id")
    )
    @Builder.Default
    private Set<Coverage> coverages = new LinkedHashSet<>();

    /**
     * Набор исключений, относящихся к этой программе. Используем таблицу program_exclusions для связи.
     */
    @ManyToMany
    @JoinTable(
            name = "program_exclusions",
            joinColumns = @JoinColumn(name = "program_id"),
            inverseJoinColumns = @JoinColumn(name = "exclusion_id")
    )
    private Set<Exclusion> exclusions = new LinkedHashSet<>();

    /**
     * Акции/скидки, применимые к этой программе.
     */
    @ManyToMany
    @JoinTable(
            name = "promo_offer_programs",
            joinColumns = @JoinColumn(name = "program_id"),
            inverseJoinColumns = @JoinColumn(name = "promo_offer_id")
    )
    @Builder.Default
    private Set<PromoOffer> promoOffers = new LinkedHashSet<>();

    /**
     * Набор регионов, в которых доступна данная программа.
     */
    @ManyToMany
    @JoinTable(
            name = "program_regions",
            joinColumns = @JoinColumn(name = "program_id"),
            inverseJoinColumns = @JoinColumn(name = "region_id")
    )
    private Set<Region> regions = new LinkedHashSet<>();

    /**
     * Набор медицинских организаций, связанных с программой.
     */
    @ManyToMany
    @JoinTable(
            name = "program_medical_orgs",
            joinColumns = @JoinColumn(name = "program_id"),
            inverseJoinColumns = @JoinColumn(name = "medical_org_id")
    )
    private Set<MedicalOrganization> medicalOrganizations = new LinkedHashSet<>();
}
