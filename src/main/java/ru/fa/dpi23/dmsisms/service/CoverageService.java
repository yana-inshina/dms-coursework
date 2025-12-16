package ru.fa.dpi23.dmsisms.service;

import org.springframework.data.domain.Sort;
import ru.fa.dpi23.dmsisms.entity.Coverage;

import java.util.List;

/**
 * Сервис для управления справочником покрытий (FR-04).
 */
public interface CoverageService {

    /**
     * Получить список покрытий с возможностью фильтрации по ключевому слову и сортировкой.
     *
     * @param keyword фильтр по части названия
     * @param sort    порядок сортировки
     * @return список покрытий
     */
    List<Coverage> findAll(String keyword, Sort sort);

    /**
     * Найти покрытие по идентификатору.
     *
     * @param id идентификатор покрытия
     * @return сущность покрытия
     */
    Coverage findById(Long id);

    /**
     * Сохранить или обновить покрытие.
     *
     * @param coverage сущность покрытия
     * @return сохранённое покрытие
     */
    Coverage save(Coverage coverage);

    /**
     * Удалить покрытие по идентификатору. Если покрытие привязано к продуктам,
     * выбрасывается IllegalStateException.
     *
     * @param id идентификатор
     */
    void deleteById(Long id);
}