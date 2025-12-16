package ru.fa.dpi23.dmsisms.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.fa.dpi23.dmsisms.entity.CorporateClient;
import ru.fa.dpi23.dmsisms.repository.CorporateClientRepository;
import ru.fa.dpi23.dmsisms.repository.RegionRepository;
import ru.fa.dpi23.dmsisms.service.RegionService;

/**
 * Контроллер для управления корпоративными клиентами.
 * Доступен менеджерам и администраторам. Позволяет просматривать список
 * корпоративных клиентов, создавать новых, редактировать и удалять.
 */
@Controller
@PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
@RequestMapping("/corp-clients")
@RequiredArgsConstructor
public class CorporateClientController {

    private final CorporateClientRepository corporateClientRepository;
    private final RegionService regionService;
    private final RegionRepository regionRepository;

    /**
     * Список корпоративных клиентов.
     */
    @GetMapping
    public String list(Model model) {
        model.addAttribute("clients", corporateClientRepository.findAll());
        return "corp-clients/list";
    }

    /**
     * Форма создания нового корпоративного клиента.
     */
    @GetMapping("/new")
    public String newClientForm(Model model) {
        model.addAttribute("client", new CorporateClient());
        // Список регионов для выбора
        model.addAttribute("regions", regionService.findAll(null, Sort.by("name")));
        model.addAttribute("formTitle", "Новый корпоративный клиент");
        // Для менеджера/админа, форма отправляется на стандартный путь сохранения
        model.addAttribute("formAction", "/corp-clients/save");
        model.addAttribute("cancelUrl", "/corp-clients");
        return "corp-clients/form";
    }

    /**
     * Форма редактирования существующего корпоративного клиента.
     */
    @GetMapping("/{id}/edit")
    public String editClientForm(@PathVariable Long id, Model model) {
        CorporateClient client = corporateClientRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Корпоративный клиент не найден: " + id));
        model.addAttribute("client", client);
        model.addAttribute("regions", regionService.findAll(null, Sort.by("name")));
        model.addAttribute("formTitle", "Редактирование корпоративного клиента");
        // Для менеджера/админа, форма отправляется на стандартный путь сохранения
        model.addAttribute("formAction", "/corp-clients/save");
        model.addAttribute("cancelUrl", "/corp-clients");
        return "corp-clients/form";
    }

    /**
     * Сохранение корпоративного клиента (создание или обновление).
     */
    @PostMapping("/save")
    public String saveClient(@ModelAttribute("client") CorporateClient client) {
        // Подгружаем регион по id, если он указан
        if (client.getRegion() != null && client.getRegion().getId() != null) {
            client.setRegion(regionRepository.findById(client.getRegion().getId()).orElse(null));
        }
        corporateClientRepository.save(client);
        return "redirect:/corp-clients";
    }

    /**
     * Удаление корпоративного клиента по id.
     */
    @PostMapping("/{id}/delete")
    public String deleteClient(@PathVariable Long id) {
        corporateClientRepository.deleteById(id);
        return "redirect:/corp-clients";
    }
}