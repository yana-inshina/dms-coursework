package ru.fa.dpi23.dmsisms.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "roles")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Храним имя роли как текст (ADMIN, MANAGER и т.д.)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private RoleName name;

    // Пустой конструктор нужен JPA
    public Role() {
    }

    // Этот конструктор нужен нам в DataInitializer:
    // new Role(RoleName.ADMIN)
    public Role(RoleName name) {
        this.name = name;
    }

    // Запасной конструктор, если вдруг понадобится
    public Role(Long id, RoleName name) {
        this.id = id;
        this.name = name;
    }

    // --- геттеры / сеттеры ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public RoleName getName() {
        return name;
    }

    public void setName(RoleName name) {
        this.name = name;
    }
}