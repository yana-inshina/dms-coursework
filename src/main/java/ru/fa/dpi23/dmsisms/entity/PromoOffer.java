package ru.fa.dpi23.dmsisms.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import ru.fa.dpi23.dmsisms.entity.InsuranceProgram;
import ru.fa.dpi23.dmsisms.entity.Product;

@Entity
@Table(name = "promo_offers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromoOffer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;          // название акции

    @Column(length = 2000)
    private String description;   // условия акции/скидки

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private DiscountType discountType; // Тип скидки (процент или фиксированная сумма)

    @Column(nullable = false)
    private BigDecimal discountAmount; // Величина скидки

    private LocalDate validFrom;  // дата начала действия акции
    private LocalDate validTo;    // дата окончания действия акции

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true; // активность акции

    /**
     * Привязка к страховым программам (InsuranceProgram). Акция может быть применима к нескольким программам.
     * Обратная сторона связи: связь управляется полем promoOffers в InsuranceProgram.
     */
    @ManyToMany(mappedBy = "promoOffers")
    private Set<InsuranceProgram> applicableToPrograms = new HashSet<>();

    // Связь с тарифными планами удалена, так как тарифы не используются.

    /**
     * Привязка акций к продуктам. Сохраняется для обратной совместимости
     * и совпадает со связью в классе Product. Связь двусторонняя, но
     * в интерфейсе не используется. При необходимости старые продукты
     * могут быть связаны с акциями через таблицу promo_offer_products.
     */
    @Builder.Default
    @ManyToMany
    @JoinTable(
            name = "promo_offer_products",
            joinColumns = @JoinColumn(name = "promo_offer_id"),
            inverseJoinColumns = @JoinColumn(name = "product_id")
    )
    private Set<Product> applicableToProducts = new HashSet<>();
}

