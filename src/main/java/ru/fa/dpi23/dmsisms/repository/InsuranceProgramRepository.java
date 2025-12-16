package ru.fa.dpi23.dmsisms.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.fa.dpi23.dmsisms.entity.InsuranceProgram;

import java.util.List;
import java.util.Optional;

public interface InsuranceProgramRepository extends JpaRepository<InsuranceProgram, Long> {

    // поиск по названию с сортировкой (для списка/поиска)
    List<InsuranceProgram> findByNameContainingIgnoreCase(String name, Sort sort);

    // ⬇⬇⬇ ЭТО как раз нужно DataInitializer'у ⬇⬇⬇
    Optional<InsuranceProgram> findByCode(String code);

    /**
     * Количество активных программ.
     *
     * <p>Метод сформирован по соглашениям Spring Data JPA и будет
     * автоматически реализован фреймворком. Возвращает число
     * страховых программ, для которых флаг {@code active} равен {@code true}.
     */
    long countByActiveTrue();
}
