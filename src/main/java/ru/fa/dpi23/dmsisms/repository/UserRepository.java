package ru.fa.dpi23.dmsisms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.fa.dpi23.dmsisms.entity.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);
}