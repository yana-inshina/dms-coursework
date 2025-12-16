package ru.fa.dpi23.dmsisms.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Entity
@Table(name = "corporate_applications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CorporateApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // организация, которая подаёт заявку
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "corporate_client_id", nullable = false)
    private CorporateClient corporateClient;

    // выбранный продукт
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "program_id", nullable = false)
    private InsuranceProgram program;
    // регион обслуживания по полису (может отличаться от региона регистрации компании)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_region_id", nullable = false)
    private Region serviceRegion;

    @Column(nullable = false)
    private Integer headcount;        // количество застрахованных сотрудников

    @Column(name = "average_age")
    private Integer averageAge;       // средний возраст (упрощённо)

    @Column(name = "age_band", length = 50)
    private String ageBand;           // если вместо среднего возраста используется бенд

    @Column(name = "base_price_snapshot", precision = 15, scale = 2)
    private BigDecimal basePriceSnapshot;   // базовая цена за 1 человека (по возрасту/бенду)

    @Column(name = "calculated_premium", precision = 15, scale = 2)
    private BigDecimal calculatedPremium;   // итоговая премия по формуле

    // Групповой полис, выпущенный по этой заявке (FR-09 + FR-10)
    @OneToOne
    @JoinColumn(name = "policy_id")
    private InsurancePolicy policy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ApplicationStatus status; // NEW / APPROVED / REJECTED / CONVERTED_TO_POLICY

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(length = 2000)
    private String comment;           // комментарий менеджера

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = ApplicationStatus.NEW;
        }
    }
}
