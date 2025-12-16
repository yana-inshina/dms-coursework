package ru.fa.dpi23.dmsisms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.fa.dpi23.dmsisms.entity.ApplicationStatus;
import ru.fa.dpi23.dmsisms.entity.InsuranceApplication;
import ru.fa.dpi23.dmsisms.entity.User;

import java.util.List;

public interface InsuranceApplicationRepository extends JpaRepository<InsuranceApplication, Long> {

    long count();

    // если где-то надо искать по статусам — можно оставить
    List<InsuranceApplication> findByStatusIn(List<ApplicationStatus> statuses);

    // Среднее время обработки заявки (в секундах) от created_at до processed_at
    // ВАРИАНТ ДЛЯ MySQL / MariaDB
    @Query(
            value = """
                    select avg(timestampdiff(
                               SECOND,
                               ia.created_at,
                               ia.processed_at
                           ))
                    from insurance_applications ia
                    where ia.processed_at is not null
                      and ia.status in ('APPROVED', 'CONVERTED_TO_POLICY')
                    """,
            nativeQuery = true
    )
    Double avgProcessingSeconds();

    /**
     * Возвращает список заявок, созданных указанным пользователем.
     * Используется для ограничения видимости заявок для обычных пользователей.
     */
    List<InsuranceApplication> findByUser(User user);
}
