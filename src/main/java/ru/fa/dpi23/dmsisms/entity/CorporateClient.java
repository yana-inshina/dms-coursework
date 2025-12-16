package ru.fa.dpi23.dmsisms.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "corporate_clients")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CorporateClient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 12, unique = true)
    private String inn;              // ИНН организации

    @Column(nullable = false, length = 255)
    private String name;             // название организации

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id", nullable = false)
    private Region region;           // регион организации

    @Column(name = "contact_person", length = 255)
    private String contactPerson;    // ФИО контактного лица

    @Column(name = "contact_email", length = 255)
    private String contactEmail;

    @Column(name = "contact_phone", length = 50)
    private String contactPhone;
}
