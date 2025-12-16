package ru.fa.dpi23.dmsisms.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Сущность заявки на страхование. Поля, связанные с тарифными планами, убраны,
 * но сохранены основные реквизиты заявки (программа, ФИО, контакты, возраст,
 * количество застрахованных лиц, наличие хронических заболеваний и т.д.).
 */
@Entity
@Table(name = "insurance_applications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InsuranceApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Выбранная программа ДМС. Поле обязательно: либо заполняется на форме
     * пользователем, либо предзаполнено через ссылку на продукт.
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "program_id")
    private InsuranceProgram program;

    /**
     * Фамилия, имя и отчество заявителя. Обязательное поле.
     */
    @Column(nullable = false, length = 200)
    private String fullName;

    /**
     * Контактный телефон заявителя. Обязательное поле.
     */
    @Column(nullable = false, length = 100)
    private String phone;

    /**
     * Электронная почта заявителя. Может быть null.
     */
    @Column(length = 150)
    private String email;

    /**
     * Дата рождения заявителя. Может быть null.
     */
    private LocalDate birthDate;

    /**
     * Количество застрахованных лиц по этой заявке. По умолчанию 1.
     */
    @Builder.Default
    @Column(nullable = false)
    private Integer insuredPersons = 1;

    /**
     * Признак наличия хронических заболеваний. По умолчанию false.
     */
    @Builder.Default
    @Column(nullable = false)
    private boolean chronicDiseases = false;

    /**
     * Дополнительный комментарий к заявке. Может быть пустым.
     */
    @Column(length = 1000)
    private String comment;

    /**
     * Связанный страховой полис. Устанавливается после выпуска полиса из заявки.
     */
    @OneToOne(mappedBy = "application")
    private InsurancePolicy policy;

    /**
     * Снимок базовой стоимости программы на момент оформления заявки. Используется для отображения пользователю
     * «чистой» базовой цены программы без региональных и возрастных коэффициентов.
     */
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal basePriceSnapshot;

    /**
     * Рассчитанная страховая премия по заявке с учётом всех коэффициентов, регионов и акций.
     */
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal calculatedPremium;

    /**
     * Регион обслуживания для заявки. Может быть null для исторических записей.
     */
    @ManyToOne(optional = true)
    @JoinColumn(name = "region_id")
    private Region region;

    /**
     * Текущий статус заявки.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private ApplicationStatus status = ApplicationStatus.NEW;

    /**
     * Время создания заявки.
     */
    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * Время обработки заявки (одобрения или отклонения). Может быть null, если заявка ещё не обработана.
     */
    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    /**
     * Пользователь, оставивший заявку. Связь много к одному. Используется для ограничения доступа к заявкам.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
}
