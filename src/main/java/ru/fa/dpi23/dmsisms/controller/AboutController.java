package ru.fa.dpi23.dmsisms.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.util.List;

@Controller
public class AboutController {

    @GetMapping("/about")
    public String about(Model model) {
        model.addAttribute("fullName", "Иншина Яна Антоновна");
        model.addAttribute("group", "ДПИ23-1");
        model.addAttribute("university", "Финансовый университет при Правительстве РФ");
        model.addAttribute("email", "237338@edu.fa.ru");

        model.addAttribute("techStack", "Java 17, Spring Boot 3, Spring Security, Spring Data JPA (Hibernate), Thymeleaf, MySQL");

        model.addAttribute("startDate", LocalDate.of(2025, 10, 1));
        model.addAttribute("endDate", LocalDate.of(2025, 12, 11));

        // краткий опыт (списком — красиво выводить)
        model.addAttribute("experiencePoints", List.of(
                "Java 17: работа с ООП, коллекциями, датами (LocalDate), обработка форм и валидация ввода.",
                "Spring Boot: организация проекта по слоям (controller/service/repository), шаблоны страниц и маршрутизация.",
                "Spring Security: аутентификация, авторизация по ролям (ADMIN/MANAGER/User), защита CSRF, выход из системы.",
                "Spring Data JPA (Hibernate): сущности, связи (ManyToOne/OneToMany), репозитории, запросы (поиск/сортировка/агрегации).",
                "Thymeleaf: динамические страницы, таблицы, формы, вывод ошибок, ограничение кнопок/ссылок по ролям.",
                "MySQL: проектирование таблиц и ограничений (unique), хранение тестовых данных, проверка целостности."
        ));

        // короткое описание проекта (как для отчёта)
        model.addAttribute("projectSummary",
                "Информационная система ДМС: ведение клиентов, страховых программ и полисов, " +
                        "поиск и сортировка записей, статистика, разграничение прав доступа и администрирование ролей.");

        // небольшой текст о себе. Это поле выводится на странице "Об авторе" под основной информацией
        model.addAttribute("authorNote", "Меня зовут Иншина Яна и я студент Финансового университета. " +
                "Этот проект создан в учебных целях для демонстрации навыков разработки веб‑приложений на Spring Boot.\n" +
                "Я старалась уделить внимание как функциональной части (расчёт тарифов, фильтры и права доступа), " +
                "так и пользовательскому интерфейсу.");

        return "about";
    }
}
