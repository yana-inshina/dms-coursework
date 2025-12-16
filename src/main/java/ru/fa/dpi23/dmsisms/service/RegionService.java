package ru.fa.dpi23.dmsisms.service;

import org.springframework.data.domain.Sort;
import ru.fa.dpi23.dmsisms.entity.Region;

import java.util.List;

public interface RegionService {

    List<Region> findAll(String keyword, Sort sort);

    Region findById(Long id);

    Region save(Region region);

    void deleteById(Long id);

    /**
     * Возвращает все регионы, отсортированные по названию по возрастанию.
     * Реализация выполняется в RegionServiceImpl. Не делаем метод default,
     * поскольку вызов default‑методов в интерфейсе может некорректно работать
     * с JDK‑прокси Spring и вызывать проблемы с получением данных. Поэтому
     * явно реализуем метод в классе‑реализации.
     */
    List<Region> findAllSortedByName();
}