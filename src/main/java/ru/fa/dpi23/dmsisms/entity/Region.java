package ru.fa.dpi23.dmsisms.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "regions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Region {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;          // название региона (Москва, СПб, Центральный и т.п.)

    @Column(length = 2000)
    private String description;   // пояснение (можно оставить пустым)

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;

    /**
     * Региональный коэффициент для расчёта премии. По умолчанию 1.0.
     * Используется в формулах для индивидуальных заявок. Для
     * корпоративных заявок коэффициент не применяется.
     */
    @Column(name = "coefficient", precision = 10, scale = 2, nullable = false)
    @Builder.Default
    private java.math.BigDecimal coefficient = java.math.BigDecimal.ONE;
}
