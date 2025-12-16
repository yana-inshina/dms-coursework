package ru.fa.dpi23.dmsisms.service;

import org.springframework.data.domain.Sort;
import ru.fa.dpi23.dmsisms.entity.Exclusion;

import java.util.List;

public interface ExclusionService {

    List<Exclusion> findAll(String keyword, Sort sort);

    Exclusion findById(Long id);

    Exclusion save(Exclusion exclusion);

    void deleteById(Long id);
}