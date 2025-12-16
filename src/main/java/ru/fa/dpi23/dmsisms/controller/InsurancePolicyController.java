package ru.fa.dpi23.dmsisms.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.fa.dpi23.dmsisms.entity.InsurancePolicy;
import ru.fa.dpi23.dmsisms.entity.PolicyStatus;
import ru.fa.dpi23.dmsisms.repository.ClientRepository;
import ru.fa.dpi23.dmsisms.repository.InsuranceProgramRepository;
import ru.fa.dpi23.dmsisms.service.InsurancePolicyService;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/policies")
@RequiredArgsConstructor
public class InsurancePolicyController {

    private final InsurancePolicyService policyService;
    private final ClientRepository clientRepository;
    private final InsuranceProgramRepository insuranceProgramRepository;

    private void fillForm(Model model, CsrfToken csrfToken, String title) {
        model.addAttribute("clients", clientRepository.findAll());
        // Отфильтровываем программу "Расширенный ДМС" из выпадающего списка программ
        var programs = insuranceProgramRepository.findAll().stream()
                .filter(p -> {
                    String name = p.getName();
                    return name == null || !name.equalsIgnoreCase("Расширенный ДМС");
                })
                .toList();
        model.addAttribute("programs", programs);
        model.addAttribute("title", title);
        model.addAttribute("_csrf", csrfToken);
        model.addAttribute("allStatuses", PolicyStatus.values());
    }

    // ===== СПИСОК ПОЛИСОВ: поиск + сортировка + фильтр по статусу =====
    @GetMapping
    public String listPolicies(@RequestParam(value = "keyword", required = false) String keyword,
                               @RequestParam(value = "sortField", required = false, defaultValue = "id") String sortField,
                               @RequestParam(value = "sortDir", required = false, defaultValue = "asc") String sortDir,
                               @RequestParam(value = "statusFilter", required = false) PolicyStatus statusFilter,
                               Model model,
                               Authentication authentication) {

        // whitelist доступных полей сортировки
        if (!List.of("id", "policyNumber", "startDate", "endDate", "premium", "status").contains(sortField)) {
            sortField = "id";
        }

        // нормализуем направление
        if (!"desc".equalsIgnoreCase(sortDir)) {
            sortDir = "asc";
        }

        // берём список полисов с учётом фильтра по статусу
        var policies = policyService.list(keyword, sortField, sortDir, statusFilter);

        // Ограничиваем доступ для обычных пользователей: показываем только полисы,
        // созданные по их собственным заявкам. Менеджеры и администраторы видят всё.
        boolean isManagerOrAdmin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_MANAGER") || a.getAuthority().equals("ROLE_ADMIN"));
        if (!isManagerOrAdmin) {
            String username = authentication != null ? authentication.getName() : null;
            if (username != null) {
                policies = policies.stream()
                        .filter(p -> p.getApplication() != null
                                && p.getApplication().getUser() != null
                                && username.equals(p.getApplication().getUser().getUsername()))
                        .toList();
            } else {
                policies = java.util.Collections.emptyList();
            }
        }

        model.addAttribute("policies", policies);
        model.addAttribute("keyword", keyword);
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reverseSortDir", "asc".equalsIgnoreCase(sortDir) ? "desc" : "asc");

        // фильтр по статусу + список всех статусов для <select>
        model.addAttribute("statusFilter", statusFilter);
        model.addAttribute("allStatuses", PolicyStatus.values());

        // остаёмся на твоём старом шаблоне
        return "policies/list";
    }

    // ===== СОЗДАНИЕ ПОЛИСА =====
    @GetMapping("/new")
    public String showCreateForm(Model model, CsrfToken csrfToken) {
        InsurancePolicy policy = new InsurancePolicy();
        policy.setStartDate(LocalDate.now());
        policy.setEndDate(LocalDate.now().plusYears(1));
        policy.setStatus(PolicyStatus.ACTIVE);

        model.addAttribute("policy", policy);
        fillForm(model, csrfToken, "Новый полис ДМС");
        return "policies/form";
    }

    @PostMapping
    public String createPolicy(@Valid @ModelAttribute("policy") InsurancePolicy policy,
                               BindingResult br,
                               Model model,
                               CsrfToken csrfToken) {

        if (policyService.isPolicyNumberTaken(policy.getPolicyNumber(), null)) {
            br.rejectValue("policyNumber", "duplicate", "Полис с таким номером уже существует");
        }

        if (br.hasErrors()) {
            fillForm(model, csrfToken, "Новый полис ДМС");
            return "policies/form";
        }

        policyService.save(policy);
        return "redirect:/policies";
    }

    // ===== РЕДАКТИРОВАНИЕ ПОЛИСА =====
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model, CsrfToken csrfToken) {
        InsurancePolicy policy = policyService.get(id);

        model.addAttribute("policy", policy);
        fillForm(model, csrfToken, "Редактирование полиса");
        return "policies/form";
    }

    @PostMapping("/{id}")
    public String updatePolicy(@PathVariable Long id,
                               @Valid @ModelAttribute("policy") InsurancePolicy policyFromForm,
                               BindingResult br,
                               Model model,
                               CsrfToken csrfToken) {

        if (policyService.isPolicyNumberTaken(policyFromForm.getPolicyNumber(), id)) {
            br.rejectValue("policyNumber", "duplicate", "Полис с таким номером уже существует");
        }

        if (br.hasErrors()) {
            fillForm(model, csrfToken, "Редактирование полиса");
            return "policies/form";
        }

        InsurancePolicy existing = policyService.get(id);
        existing.setPolicyNumber(policyFromForm.getPolicyNumber());
        existing.setClient(policyFromForm.getClient());
        existing.setProgram(policyFromForm.getProgram());
        existing.setStartDate(policyFromForm.getStartDate());
        existing.setEndDate(policyFromForm.getEndDate());
        existing.setPremium(policyFromForm.getPremium());
        existing.setStatus(policyFromForm.getStatus());

        policyService.save(existing);
        return "redirect:/policies";
    }

    // ===== УДАЛЕНИЕ =====
    @PostMapping("/{id}/delete")
    public String deletePolicy(@PathVariable Long id,
                               org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        try {
            policyService.delete(id);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            // If there are foreign key constraints (e.g. the policy is referenced by
            // a corporate or individual application), deletion will cause a
            // DataIntegrityViolationException. Catch it and inform the user
            // instead of letting the exception propagate as an HTTP 500.
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Нельзя удалить полис, так как существуют связанные заявки или другие сущности.");
        }
        return "redirect:/policies";
    }

    // ===== ПРОСМОТР ПОЛИСА =====
    /**
     * Показывает карточку полиса в режиме просмотра (без возможности редактирования).
     * Доступно для всех авторизованных пользователей. Менеджеры и администраторы
     * могут перейти к редактированию с этого экрана через соответствующую кнопку.
     */
    @GetMapping("/{id}/view")
    public String viewPolicy(@PathVariable Long id, Model model) {
        InsurancePolicy policy = policyService.get(id);
        model.addAttribute("item", policy);
        model.addAttribute("title", "Полис #" + id);
        return "policies/view";
    }

    // редирект /policies/{id} → /policies/{id}/view
    @GetMapping("/{id}")
    public String redirectToView(@PathVariable Long id) {
        return "redirect:/policies/" + id + "/view";
    }
}
