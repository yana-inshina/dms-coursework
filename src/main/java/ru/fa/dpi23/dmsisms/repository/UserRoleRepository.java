package ru.fa.dpi23.dmsisms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.fa.dpi23.dmsisms.entity.Role;
import ru.fa.dpi23.dmsisms.entity.User;
import ru.fa.dpi23.dmsisms.entity.UserRole;

public interface UserRoleRepository extends JpaRepository<UserRole, Long> {

    boolean existsByUserAndRole(User user, Role role);
}
