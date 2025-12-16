package ru.fa.dpi23.dmsisms.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.HashSet;
import java.util.Set;
import ru.fa.dpi23.dmsisms.entity.Product;

@Entity
@Table(name = "exclusions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Exclusion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;          // что исключается

    @Column(length = 2000)
    private String description;   // подробности исключения

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;

    // Привязка исключения к продуктам. Если исключение используется хотя бы в одном продукте,
    // удалить его нельзя. Используем mappedBy, поскольку таблица связи определяется в Product.
    @Builder.Default
    @ManyToMany(mappedBy = "exclusions")
    private Set<Product> products = new HashSet<>();
}
