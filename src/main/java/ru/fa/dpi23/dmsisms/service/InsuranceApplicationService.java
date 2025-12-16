package ru.fa.dpi23.dmsisms.service;

import ru.fa.dpi23.dmsisms.entity.InsuranceApplication;
import ru.fa.dpi23.dmsisms.entity.InsurancePolicy;
import ru.fa.dpi23.dmsisms.entity.InsuranceProgram;
import ru.fa.dpi23.dmsisms.entity.User;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface InsuranceApplicationService {

    // расчёт премии (FR-05)
    /**
     * Расчёт индивидуальной премии для заявки.
     *
     * @param program        выбранная программа ДМС
     * @param birthDate      дата рождения застрахованного (может быть null)
     * @param chronic        наличие хронических заболеваний
     * @param insuredPersons количество застрахованных лиц
     * @return рассчитанная сумма премии
     */
    BigDecimal calculatePremium(InsuranceProgram program,
                                LocalDate birthDate,
                                boolean chronic,
                                int insuredPersons);

    // создание заявки с расчётом премии
    InsuranceApplication createApplication(InsuranceApplication draft);

    // базовые методы списка / просмотра
    List<InsuranceApplication> findAll();

    /**
     * Возвращает заявки, созданные указанным пользователем.
     *
     * @param user владелец заявок
     * @return список заявок пользователя
     */
    List<InsuranceApplication> findByUser(User user);

    InsuranceApplication findById(Long id);

    // смена статуса
    /**
     * Одобрение заявки менеджером с опциональным комментарием.
     *
     * @param id      идентификатор заявки
     * @param comment комментарий менеджера (может быть null)
     * @return обновлённая заявка
     */
    InsuranceApplication approve(Long id, String comment);

    /**
     * Отклонение заявки менеджером с опциональным комментарием.
     *
     * @param id      идентификатор заявки
     * @param comment комментарий менеджера (может быть null)
     * @return обновлённая заявка
     */
    InsuranceApplication reject(Long id, String comment);

    // выпуск полиса из заявки
    InsurancePolicy convertToPolicy(Long id);
}
