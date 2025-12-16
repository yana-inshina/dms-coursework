package ru.fa.dpi23.dmsisms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.fa.dpi23.dmsisms.entity.Role;
import ru.fa.dpi23.dmsisms.entity.RoleName;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(RoleName name);
}