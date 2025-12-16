package ru.fa.dpi23.dmsisms.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.HashSet;
import java.util.Set;
import ru.fa.dpi23.dmsisms.entity.Product;

@Entity
@Table(name = "coverages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Coverage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;          // название покрытия (услуга/заболевание)

    @Column(length = 2000)
    private String description;   // описание/пояснение

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;

    // Привязка покрытия к продуктам. Если покрытие используется хотя бы в одном продукте,
    // удалить его нельзя. Используем mappedBy, поскольку таблица связи определяется в Product.
    @Builder.Default
    @ManyToMany(mappedBy = "coverages")
    private Set<Product> products = new HashSet<>();
}
