package ru.fa.dpi23.dmsisms.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.fa.dpi23.dmsisms.entity.Coverage;

import java.util.List;

public interface CoverageRepository extends JpaRepository<Coverage, Long> {

    List<Coverage> findByNameContainingIgnoreCase(String name, Sort sort);
}
