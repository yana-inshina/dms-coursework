package ru.fa.dpi23.dmsisms.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.fa.dpi23.dmsisms.entity.CorporateClient;
import ru.fa.dpi23.dmsisms.entity.User;
import ru.fa.dpi23.dmsisms.repository.CorporateClientRepository;
import ru.fa.dpi23.dmsisms.repository.RegionRepository;
import ru.fa.dpi23.dmsisms.repository.UserRepository;
import ru.fa.dpi23.dmsisms.service.RegionService;

/**
 * Контроллер для корпоративных пользователей, позволяющий создать
 * карточку своей организации. После создания организации корпоративный
 * клиент сможет использовать её для подачи заявок на ДМС.
 */
@Controller
@RequiredArgsConstructor
@PreAuthorize("hasRole('CORPORATE')")
public class CorporateSelfController {

    private final CorporateClientRepository corporateClientRepository;
    private final RegionService regionService;
    private final RegionRepository regionRepository;
    private final UserRepository userRepository;

    /**
     * Форма создания новой организации для корпоративного пользователя.
     */
    @GetMapping("/my-org/new")
    public String newOrgForm(Model model, Authentication authentication) {
        // Подготовка нового клиента
        CorporateClient client = new CorporateClient();
        model.addAttribute("client", client);
        model.addAttribute("regions", regionService.findAll(null, Sort.by("name")));
        model.addAttribute("formTitle", "Моя организация");
        // Путь, куда будет отправлена форма
        model.addAttribute("formAction", "/my-org/save");
        // Возврат после отмены — на главную страницу. Для корпоративного пользователя
        // переходить на список клиентов не имеет смысла, так как у него нет доступа к управлению
        // организациями. Поэтому указываем корень сайта (личный кабинет).
        model.addAttribute("cancelUrl", "/");
        return "corp-clients/form";
    }

    /**
     * Сохранение организации, созданной корпоративным пользователем.
     */
    @PostMapping("/my-org/save")
    public String saveOrg(@ModelAttribute("client") CorporateClient client,
                          Authentication authentication,
                          org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        // Подгружаем регион по id, если он указан
        if (client.getRegion() != null && client.getRegion().getId() != null) {
            client.setRegion(regionRepository.findById(client.getRegion().getId()).orElse(null));
        }
        try {
            corporateClientRepository.save(client);
        } catch (org.springframework.dao.DataIntegrityViolationException ex) {
            // Нарушение уникальных ограничений (например, ИНН уже существует)
            redirectAttributes.addFlashAttribute("errorMessage", "Организация с таким ИНН или контактным email уже существует.");
            // возвращаем пользователя на форму создания
            return "redirect:/my-org/new";
        }

        // После сохранения связываем пользователя и организацию через email.
        // Если у пользователя нет email, устанавливаем contactEmail как email пользователя.
        if (authentication != null) {
            // После сохранения обязательно синхронизируем email текущего пользователя
            // с контактным email созданной организации. Это необходимо, чтобы
            // корпоративный пользователь мог видеть свои заявки в личном кабинете.
            String username = authentication.getName();
            userRepository.findByUsername(username).ifPresent(user -> {
                // Если контактный email организации пустой, не делаем обновления
                String newEmail = client.getContactEmail();
                if (newEmail != null && !newEmail.isBlank()) {
                    user.setEmail(newEmail);
                    userRepository.save(user);
                }
            });
        }
        // Передаём флаг через параметр в URL, чтобы вывести сообщение об успешном создании
        return "redirect:/corporate-applications?orgSuccess=true";
    }

    /**
     * Обработка GET‑запросов на адрес сохранения.
     *
     * <p>Если пользователь случайно перейдёт по ссылке "/my-org/save" в адресной строке
     * браузера (GET-запрос), перенаправляем его на страницу создания организации. Это
     * предотвращает ошибку 405 «Метод не разрешён», которая раньше появлялась при
     * попытке создать карточку организации.</p>
     */
    @GetMapping("/my-org/save")
    public String redirectSaveGet() {
        return "redirect:/my-org/new";
    }

    /**
     * Форма редактирования существующей организации для корпоративного пользователя.
     *
     * <p>Позволяет пользователю изменить данные ранее сохранённой организации. Организация
     * определяется по contactEmail текущего пользователя. Если организация не найдена,
     * перенаправляем на форму создания новой организации.</p>
     */
    @GetMapping("/my-org/edit")
    public String editOrgForm(Model model, Authentication authentication) {
        if (authentication == null) {
            // если по какой‑то причине отсутствует аутентификация, перенаправляем на форму создания
            return "redirect:/my-org/new";
        }
        String username = authentication.getName();
        // Получаем пользователя, чтобы извлечь email
        User user = userRepository.findByUsername(username)
                .orElse(null);
        if (user == null || user.getEmail() == null || user.getEmail().isBlank()) {
            // нет email — перенаправляем на создание
            return "redirect:/my-org/new";
        }
        // Ищем корпоративного клиента по contactEmail
        CorporateClient client = corporateClientRepository
                .findFirstByContactEmailOrderByIdAsc(user.getEmail())
                .orElse(null);
        if (client == null) {
            return "redirect:/my-org/new";
        }
        // Заполняем модель
        model.addAttribute("client", client);
        model.addAttribute("regions", regionService.findAll(null, Sort.by("name")));
        model.addAttribute("formTitle", "Моя организация");
        // Сохраняем действие формы: обновление
        model.addAttribute("formAction", "/my-org/update");
        // Кнопка отмены переводит на главную
        model.addAttribute("cancelUrl", "/");
        return "corp-clients/form";
    }

    /**
     * Обновление существующей организации.
     *
     * <p>Данные принимаются из формы редактирования. Регион подгружается отдельно. После
     * сохранения организация обновляется в базе. Затем перенаправляем корпоративного
     * пользователя на страницу списка корпоративных заявок.</p>
     */
    @PostMapping("/my-org/update")
    public String updateOrg(@ModelAttribute("client") CorporateClient client,
                            Authentication authentication,
                            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        // подгружаем регион по id, если указан
        if (client.getRegion() != null && client.getRegion().getId() != null) {
            client.setRegion(regionRepository.findById(client.getRegion().getId()).orElse(null));
        }
        try {
            corporateClientRepository.save(client);
        } catch (org.springframework.dao.DataIntegrityViolationException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Не удалось обновить организацию. Проверьте введённые данные.");
            return "redirect:/my-org/edit";
        }
        // Обновляем email пользователя, чтобы он совпадал с контактным email организации.
        // Это гарантирует, что корпоративный клиент будет привязан к своей организации
        // независимо от того, был ли его email заполнен ранее или нет.
        if (authentication != null) {
            String username = authentication.getName();
            userRepository.findByUsername(username).ifPresent(user -> {
                String newEmail = client.getContactEmail();
                if (newEmail != null && !newEmail.isBlank()) {
                    user.setEmail(newEmail);
                    userRepository.save(user);
                }
            });
        }
        redirectAttributes.addFlashAttribute("orgUpdated", true);
        return "redirect:/corporate-applications";
    }
}