package ru.fa.dpi23.dmsisms.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.fa.dpi23.dmsisms.entity.InsuranceApplication;
import ru.fa.dpi23.dmsisms.entity.InsurancePolicy;
import ru.fa.dpi23.dmsisms.entity.Client;
import ru.fa.dpi23.dmsisms.service.ClientService;
import ru.fa.dpi23.dmsisms.entity.InsuranceProgram;
import ru.fa.dpi23.dmsisms.service.InsuranceApplicationService;
import ru.fa.dpi23.dmsisms.repository.InsuranceProgramRepository;
import ru.fa.dpi23.dmsisms.repository.UserRepository;
import ru.fa.dpi23.dmsisms.service.RegionService;

import java.util.List;

@Controller
@RequestMapping("/applications")
@RequiredArgsConstructor
public class InsuranceApplicationController {

    private final InsuranceApplicationService applicationService;
    private final InsuranceProgramRepository programRepository;
    private final RegionService regionService;
    private final UserRepository userRepository;

    private final ClientService clientService;

    // ===== список заявок =====
    @GetMapping
    public String list(Model model, Authentication authentication) {
        List<InsuranceApplication> apps;
        // Определяем, является ли пользователь менеджером или администратором
        boolean isManagerOrAdmin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_MANAGER") || a.getAuthority().equals("ROLE_ADMIN"));

        if (isManagerOrAdmin) {
            apps = applicationService.findAll();
        } else {
            // Для обычного пользователя загружаем только его заявки
            if (authentication != null) {
                apps = userRepository.findByUsername(authentication.getName())
                        .map(applicationService::findByUser)
                        .orElse(java.util.Collections.emptyList());
            } else {
                apps = java.util.Collections.emptyList();
            }
        }

        model.addAttribute("title", "Заявки по ДМС");
        model.addAttribute("items", apps);
        return "applications/applications-list";
    }

    // ===== форма создания заявки (из продукта или просто по программе) =====
    // /applications/new        – без programId (пользователь выбирает сам или просто пустая)
    // /applications/new?programId=3 – предзаполненная программа
    @GetMapping("/new")
    public String showCreateForm(
            @RequestParam(value = "programId", required = false) Long programId,
            Model model,
            org.springframework.security.core.Authentication authentication
    ) {
        InsuranceApplication app = new InsuranceApplication();

        InsuranceProgram program = null;
        if (programId != null) {
            program = programRepository.findById(programId)
                    .orElseThrow(() -> new IllegalArgumentException("Программа не найдена: " + programId));
            app.setProgram(program);
        }

        model.addAttribute("title", "Заявка на программу ДМС");
        model.addAttribute("program", program);
        model.addAttribute("item", app);

        // Справочники: все программы и регионы. Тарифные планы больше не выбираются пользователем.
        model.addAttribute("allPrograms", programRepository.findAll());
        model.addAttribute("allRegions", regionService.findAll("", org.springframework.data.domain.Sort.by("name")));


        // список клиентов для выбора: менеджер/администратор видит всех, обычный пользователь – только себя
        boolean isManagerOrAdmin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_MANAGER") || a.getAuthority().equals("ROLE_ADMIN"));
        if (isManagerOrAdmin) {
            model.addAttribute("allClients", clientService.list(null, "fullName", "asc"));
        } else {
            // Загружаем всех клиентов и фильтруем по текущему пользователю
            java.util.List<ru.fa.dpi23.dmsisms.entity.Client> all = clientService.list(null, "fullName", "asc");
            java.util.List<ru.fa.dpi23.dmsisms.entity.Client> filtered = new java.util.ArrayList<>();
            if (authentication != null) {
                userRepository.findByUsername(authentication.getName()).ifPresent(u -> {
                    String userEmail = u.getEmail();
                    String userFullName = u.getFullName();
                    // Сначала пытаемся сопоставить по email
                    if (userEmail != null && !userEmail.isBlank()) {
                        for (ru.fa.dpi23.dmsisms.entity.Client c : all) {
                            if (c.getEmail() != null && userEmail.equalsIgnoreCase(c.getEmail())) {
                                filtered.add(c);
                                break;
                            }
                        }
                    }
                    // Если по email не нашли, сравниваем по ФИО
                    if (filtered.isEmpty() && userFullName != null && !userFullName.isBlank()) {
                        for (ru.fa.dpi23.dmsisms.entity.Client c : all) {
                            if (c.getFullName() != null && userFullName.equalsIgnoreCase(c.getFullName())) {
                                filtered.add(c);
                                break;
                            }
                        }
                    }
                });
            }
            model.addAttribute("allClients", filtered);
        }

        return "applications/application-form";
    }

    // ===== сохранение заявки =====
    @PostMapping("/save")
    public String save(@ModelAttribute("item") InsuranceApplication app,
                       @RequestParam(value = "clientId", required = false) Long clientId,
                       org.springframework.security.core.Authentication authentication) {
        // Определяем, является ли пользователь менеджером или администратором
        boolean isManagerOrAdmin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_MANAGER") || a.getAuthority().equals("ROLE_ADMIN"));

        Client client = null;
        if (isManagerOrAdmin) {
            // Для менеджера/админа клиент обязателен
            if (clientId == null) {
                throw new IllegalArgumentException("Клиент не выбран");
            }
            client = clientService.get(clientId);
            if (client == null) {
                throw new IllegalArgumentException("Клиент не найден: " + clientId);
            }
            // Заполняем поля заявки данными выбранного клиента. Если у клиента
            // отсутствуют ФИО или телефон, используем email или иное
            // доступное поле, чтобы не нарушить ограничения NOT NULL в БД.
            String fn = client.getFullName();
            if (fn == null || fn.isBlank()) {
                if (client.getEmail() != null && !client.getEmail().isBlank()) {
                    fn = client.getEmail();
                } else if (client.getPhone() != null && !client.getPhone().isBlank()) {
                    fn = client.getPhone();
                } else {
                    fn = "Клиент";
                }
            }
            String phone = client.getPhone();
            if (phone == null || phone.isBlank()) {
                // если телефона нет — используем email
                phone = client.getEmail() != null ? client.getEmail() : "-";
            }
            app.setFullName(fn);
            app.setPhone(phone);
            app.setEmail(client.getEmail());
        } else {
            // Для обычного пользователя берём данные из формы и создаём (или находим) клиента по этим данным
            client = clientService.findOrCreateFromApplication(app);
        }

        InsuranceProgram program = programRepository.findById(app.getProgram().getId())
                .orElseThrow(() -> new IllegalArgumentException("Программа не найдена"));
        app.setProgram(program);

        // тарифные планы не используются, поэтому ничего не делаем
        // Обновляем регион по ID (конвертеры могут не загрузить все данные)
        if (app.getRegion() != null && app.getRegion().getId() != null) {
            app.setRegion(regionService.findById(app.getRegion().getId()));
        }

        // Привязываем заявку к текущему пользователю, чтобы ограничивать доступ
        if (authentication != null) {
            userRepository.findByUsername(authentication.getName())
                    .ifPresent(app::setUser);
        }

        InsuranceApplication saved = applicationService.createApplication(app);
        return "redirect:/applications/" + saved.getId();
    }

    // ===== просмотр заявки =====
    // ВАЖНО: только цифры, чтобы /applications/new не попадал сюда
    @GetMapping("/{id:\\d+}")
    public String view(@PathVariable Long id, Model model) {
        InsuranceApplication app = applicationService.findById(id);
        model.addAttribute("title", "Заявка #" + id);
        model.addAttribute("item", app);
        return "applications/application-view";
    }

    // ===== одобрить =====
    @PostMapping("/{id:\\d+}/approve")
    public String approve(@PathVariable Long id,
                          @RequestParam(required = false) String comment) {
        applicationService.approve(id, comment);
        return "redirect:/applications/" + id;
    }

    // ===== отклонить =====
    @PostMapping("/{id:\\d+}/reject")
    public String reject(@PathVariable Long id,
                         @RequestParam(required = false) String comment) {
        applicationService.reject(id, comment);
        return "redirect:/applications/" + id;
    }

    // ===== выпустить полис из заявки =====
    @PostMapping("/{id:\\d+}/to-policy")
    public String convertToPolicy(@PathVariable Long id) {
        InsurancePolicy policy = applicationService.convertToPolicy(id);
        return "redirect:/policies/" + policy.getId();
    }
}
