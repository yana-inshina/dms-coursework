package ru.fa.dpi23.dmsisms.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.fa.dpi23.dmsisms.entity.Client;

import java.util.List;
import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {

    List<Client> findByFullNameContainingIgnoreCase(String keyword, Sort sort);

    Optional<Client> findFirstByEmailOrderByIdAsc(String email);

    // 👇 добавляем вот этот метод
    Optional<Client> findFirstByFullNameOrderByIdAsc(String fullName);
}
