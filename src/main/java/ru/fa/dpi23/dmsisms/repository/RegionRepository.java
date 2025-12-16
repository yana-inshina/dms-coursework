package ru.fa.dpi23.dmsisms.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.fa.dpi23.dmsisms.entity.Region;

import java.util.List;

public interface RegionRepository extends JpaRepository<Region, Long> {

    List<Region> findByNameContainingIgnoreCase(String name, Sort sort);
}
