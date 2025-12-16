package ru.fa.dpi23.dmsisms.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Сущность продукта, объединяющего набор программ, покрытий и медицинских организаций.
 * Тарифные планы удалены, но сохраняется привязка к акциям, покрытиям и исключениям.
 */
@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Название продукта.
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * Описание продукта. Может быть пустым.
     */
    @Column(length = 1000)
    private String description;

    /**
     * Акции, привязанные к данному продукту. Управление связью идёт со стороны PromoOffer.
     */
    @ManyToMany(mappedBy = "applicableToProducts")
    private Set<PromoOffer> promoOffers = new HashSet<>();

    /**
     * Набор покрытий, относящихся к данному продукту. Используем таблицу product_coverages.
     */
    @Builder.Default
    @ManyToMany
    @JoinTable(
            name = "product_coverages",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "coverage_id")
    )
    private Set<Coverage> coverages = new HashSet<>();

    /**
     * Набор исключений, относящихся к данному продукту. Используем таблицу product_exclusions.
     */
    @Builder.Default
    @ManyToMany
    @JoinTable(
            name = "product_exclusions",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "exclusion_id")
    )
    private Set<Exclusion> exclusions = new HashSet<>();

    /**
     * Набор медицинских организаций, связанных с продуктом.
     */
    @Builder.Default
    @ManyToMany
    @JoinTable(
            name = "product_medical_orgs",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "medical_org_id")
    )
    private Set<MedicalOrganization> medicalOrganizations = new HashSet<>();
}
