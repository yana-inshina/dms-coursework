package ru.fa.dpi23.dmsisms.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(
        name = "insurance_policies",
        uniqueConstraints = @UniqueConstraint(columnNames = "policy_number")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InsurancePolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Номер полиса обязателен")
    @Size(max = 50, message = "Номер полиса должен быть не длиннее 50 символов")
    @Column(name = "policy_number", nullable = false, unique = true, length = 50)
    private String policyNumber;

    @NotNull(message = "Выберите клиента")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    private Client client;

    @NotNull(message = "Выберите программу страхования")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "program_id")
    private InsuranceProgram program;

    @NotNull(message = "Укажите дату начала")
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @NotNull(message = "Укажите дату окончания")
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @NotNull(message = "Укажите премию")
    @DecimalMin(value = "0.01", message = "Премия должна быть больше 0")
    @Digits(integer = 10, fraction = 2, message = "Премия должна быть в формате 1234567890.00")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal premium;

    @NotNull(message = "Выберите статус")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PolicyStatus status;

    @OneToOne
    @JoinColumn(name = "application_id")
    private InsuranceApplication application;

    // Кросс-полевая проверка: дата окончания не раньше даты начала
    @AssertTrue(message = "Дата окончания не может быть раньше даты начала")
    public boolean isDatesValid() {
        if (startDate == null || endDate == null) return true; // null проверят @NotNull
        return !endDate.isBefore(startDate);
    }
}
