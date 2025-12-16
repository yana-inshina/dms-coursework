package ru.fa.dpi23.dmsisms.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.HashSet;
import java.util.Set;
import ru.fa.dpi23.dmsisms.entity.Region;
import ru.fa.dpi23.dmsisms.entity.Product;
import jakarta.persistence.ConstraintMode;
import jakarta.persistence.ForeignKey;

@Entity
@Table(name = "medical_organizations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicalOrganization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Название медицинской организации.
     */
    @Column(nullable = false, length = 255)
    private String name;

    /**
     * ОГРН (основной государственный регистрационный номер). Обязательное поле.
     */
    @Column(nullable = false, length = 20)
    private String ogrn;

    /**
     * Адрес организации.
     */
    @Column(length = 500)
    private String address;

    /**
     * Телефон для связи.
     */
    @Column(length = 100)
    private String phone;

    /**
     * Регион, в котором находится медорганизация. Может быть null в базе, поскольку
     * в существующих данных нет валидных регионов; однако сохранение без региона
     * не допускается на уровне сервиса (FR-07). Убираем жесткий foreign key и
     * not-null ограничение, чтобы Hibernate не пытался создать FK и не падал
     * при отсутствии региона.
     */
    @ManyToOne
    @org.hibernate.annotations.NotFound(action = org.hibernate.annotations.NotFoundAction.IGNORE)
    @JoinColumn(name = "region_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Region region;

    /**
     * Описание/примечание (необязательно).
     */
    @Column(length = 2000)
    private String description;

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;

    /**
     * Продукты, в которых участвует данная медорганизация. Нужна для проверки
     * зависимостей при удалении (FR-07).
     */
    @Builder.Default
    @ManyToMany(mappedBy = "medicalOrganizations")
    private Set<Product> products = new HashSet<>();
}
