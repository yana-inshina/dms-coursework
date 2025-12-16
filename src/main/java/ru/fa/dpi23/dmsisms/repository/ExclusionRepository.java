package ru.fa.dpi23.dmsisms.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.fa.dpi23.dmsisms.entity.Exclusion;

import java.util.List;

public interface ExclusionRepository extends JpaRepository<Exclusion, Long> {

    List<Exclusion> findByNameContainingIgnoreCase(String name, Sort sort);
}
