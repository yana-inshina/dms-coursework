package ru.fa.dpi23.dmsisms.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.fa.dpi23.dmsisms.entity.CorporateApplication;
import ru.fa.dpi23.dmsisms.entity.CorporateClient;
import ru.fa.dpi23.dmsisms.entity.InsuranceProgram;
import ru.fa.dpi23.dmsisms.entity.Region;
import ru.fa.dpi23.dmsisms.entity.User;
import ru.fa.dpi23.dmsisms.service.CorporateApplicationService;
import ru.fa.dpi23.dmsisms.repository.CorporateClientRepository;
import ru.fa.dpi23.dmsisms.repository.InsuranceProgramRepository;
import ru.fa.dpi23.dmsisms.repository.RegionRepository;

import java.util.List;

@Controller
// Используем более понятный путь, чтобы адрес страницы корпоративных заявок
// выглядел как «/corporate-applications» вместо аббревиатуры.
@RequestMapping("/corporate-applications")
@RequiredArgsConstructor
@org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('ADMIN','MANAGER','CORPORATE')")
public class CorporateApplicationController {

    private final CorporateApplicationService corporateApplicationService;
    private final CorporateClientRepository corporateClientRepository;
    private final InsuranceProgramRepository insuranceProgramRepository;
    // Tariff plans are no longer selected by the user. They will be assigned automatically.
    // Тарифные планы более не используются
    private final RegionRepository regionRepository;
    // Repository for looking up the current user by username
    private final ru.fa.dpi23.dmsisms.repository.UserRepository userRepository;

    // ====== список заявок ======
    @GetMapping
    public String list(@RequestParam(required = false) Boolean orgSuccess,
                       Model model,
                       Authentication authentication) {

        // Инициализируем noClient значением false, чтобы избежать NullPointerException
        // в шаблоне. Если у корпоративного пользователя не найден клиент, это
        // значение будет заменено на true ниже.
        model.addAttribute("noClient", false);
        // Сообщение об успешном создании организации через параметр в URL
        if (orgSuccess != null && orgSuccess) {
            model.addAttribute("orgSuccess", true);
        }

        boolean isCorporate = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_CORPORATE"));

        List<CorporateApplication> items;

        if (isCorporate) {
            // Для корпоративного пользователя ищем клиента по email аккаунта пользователя
            User user = userRepository.findByUsername(authentication.getName())
                    .orElseThrow(() -> new IllegalStateException(
                            "Пользователь не найден: " + authentication.getName()));
            String userEmail = user.getEmail();
            // ищем корпоративного клиента, у которого контактный email совпадает с email пользователя
            CorporateClient client = null;
            if (userEmail != null && !userEmail.isBlank()) {
                client = corporateClientRepository
                        .findFirstByContactEmailOrderByIdAsc(userEmail)
                        .orElse(null);
            }
            if (client == null) {
                // если корпоративный клиент не найден, просто отображаем пустой список
                // и устанавливаем флаг для вывода сообщения пользователю
                model.addAttribute("noClient", true);
                items = java.util.Collections.emptyList();
            } else {
                // передаём найденную организацию для возможного отображения в шаблоне
                model.addAttribute("myClient", client);
                items = corporateApplicationService.findByClient(client);
            }
        } else {
            // MANAGER / ADMIN видят все заявки
            items = corporateApplicationService.findAll();
        }

        model.addAttribute("items", items);
        return "corp-applications/list"; // твой thymeleaf шаблон списка
    }

    // ====== форма создания ======
    @GetMapping("/new")
    public String createForm(@RequestParam(required = false) Long programId, Model model) {

        CorporateApplication app = new CorporateApplication();

        if (programId != null) {
            InsuranceProgram program = insuranceProgramRepository.findById(programId)
                    .orElseThrow(() -> new IllegalArgumentException("Программа не найдена: " + programId));
            app.setProgram(program);
        }

        model.addAttribute("item", app);
        // список организаций для выбора в форме. Имя атрибута должно совпадать
        // с используемым в шаблоне (corpClients)
        model.addAttribute("corpClients", corporateClientRepository.findAll());

        // Исключаем программу "Расширенный ДМС" из выпадающего списка
        var programs = insuranceProgramRepository.findAll().stream()
                .filter(p -> {
                    String name = p.getName();
                    return name == null || !name.equalsIgnoreCase("Расширенный ДМС");
                })
                .toList();
        model.addAttribute("programs", programs);
        // Тарифные планы больше не выбираются пользователем, поэтому не передаём их в модель
        model.addAttribute("regions", regionRepository.findAll());

        return "corp-applications/form"; // твой шаблон формы
    }

    // ====== сохранение через сервис (FR-09) ======
    @PostMapping("/save")
    public String save(@ModelAttribute("item") CorporateApplication form) {

        // подтягиваем связанные сущности по id
        CorporateClient client = corporateClientRepository.findById(
                        form.getCorporateClient().getId())
                .orElseThrow(() -> new IllegalArgumentException("Клиент не найден"));

        InsuranceProgram program = insuranceProgramRepository.findById(
                        form.getProgram().getId())
                .orElseThrow(() -> new IllegalArgumentException("Программа не найдена"));

        // Тарифный план больше не назначается. Расчёт премии будет основан на программе и возрастном коэффициенте.

        Region region = regionRepository.findById(
                        form.getServiceRegion().getId())
                .orElseThrow(() -> new IllegalArgumentException("Регион не найден"));

        CorporateApplication draft = CorporateApplication.builder()
                .id(form.getId())                 // если редактирование
                .corporateClient(client)
                .program(program)
                .serviceRegion(region)
                .headcount(form.getHeadcount())
                .averageAge(form.getAverageAge())
                .ageBand(form.getAgeBand())
                .comment(form.getComment())
                .build();

        CorporateApplication saved = corporateApplicationService.createOrUpdate(draft);

        // После сохранения перенаправляем на просмотр заявки с параметром, указывающим на успешное создание.
        return "redirect:/corporate-applications/" + saved.getId() + "?appSuccess=true";
    }

    // ====== просмотр карточки заявки ======
    @GetMapping("/{id}")
    public String view(@PathVariable Long id,
                       @RequestParam(required = false) Boolean appSuccess,
                       Model model) {
        CorporateApplication app = corporateApplicationService.findById(id);
        model.addAttribute("item", app);
        if (appSuccess != null && appSuccess) {
            model.addAttribute("appSuccess", true);
        }
        // Возвращаем имя шаблона в папке corp-applications. Ранее имя было с подчёркиванием,
        // что приводило к ошибке разрешения ресурса (500). Теперь используем дефис,
        // соответствующий реальному пути файла в src/main/resources/templates.
        return "corp-applications/view";
    }

    // ====== действия менеджера ======
    @PostMapping("/{id}/approve")
    public String approve(@PathVariable Long id,
                          @RequestParam(required = false) String comment) {
        corporateApplicationService.approve(id, comment);
        return "redirect:/corporate-applications/" + id;
    }

    @PostMapping("/{id}/reject")
    public String reject(@PathVariable Long id,
                         @RequestParam(required = false) String comment) {
        corporateApplicationService.reject(id, comment);
        return "redirect:/corporate-applications/" + id;
    }

    @PostMapping("/{id}/convert")
    public String convertToPolicy(@PathVariable Long id) {
        corporateApplicationService.convertToPolicy(id);
        return "redirect:/corporate-applications/" + id;
    }
}
