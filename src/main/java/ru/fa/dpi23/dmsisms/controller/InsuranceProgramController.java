package ru.fa.dpi23.dmsisms.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.fa.dpi23.dmsisms.entity.InsuranceProgram;
import ru.fa.dpi23.dmsisms.repository.InsuranceProgramRepository;
import ru.fa.dpi23.dmsisms.service.CoverageService;
import ru.fa.dpi23.dmsisms.service.ExclusionService;
import ru.fa.dpi23.dmsisms.service.PromoOfferService;
import ru.fa.dpi23.dmsisms.service.RegionService;
import ru.fa.dpi23.dmsisms.service.MedicalOrganizationService;

import java.util.List;

@Controller
@RequestMapping("/programs")
@RequiredArgsConstructor
public class InsuranceProgramController {

    private final InsuranceProgramRepository insuranceProgramRepository;
    private final CoverageService coverageService;
    private final ExclusionService exclusionService;
    private final PromoOfferService promoOfferService;
    private final RegionService regionService;
    private final MedicalOrganizationService medicalOrganizationService;

    @GetMapping
    public String listPrograms(@RequestParam(value = "keyword", required = false) String keyword,
                               @RequestParam(value = "sortField", defaultValue = "name") String sortField,
                               @RequestParam(value = "sortDir", defaultValue = "asc") String sortDir,
                               @RequestParam(value = "activeFilter", required = false) String activeFilter,
                               @RequestParam(value = "regionId", required = false) Long regionId,
                               @RequestParam(value = "coverageId", required = false) Long coverageId,
                               @RequestParam(value = "promo", required = false) String promo,
                               @RequestParam(value = "page", defaultValue = "1") Integer page,
                               Model model) {

        // защита от "левых" полей
        if (!List.of("name", "code", "basePrice", "active", "id").contains(sortField)) {
            sortField = "name";
        }

        Sort sort = Sort.by(sortField);
        sort = "asc".equalsIgnoreCase(sortDir) ? sort.ascending() : sort.descending();

        List<InsuranceProgram> programs;

        // если keyword НЕ пустой — ищем; иначе показываем все
        if (keyword != null && !keyword.isBlank()) {
            programs = insuranceProgramRepository.findByNameContainingIgnoreCase(keyword, sort);
            model.addAttribute("keyword", keyword);
        } else {
            programs = insuranceProgramRepository.findAll(sort);
        }

        // Исключаем из выдачи программу "Расширенный ДМС" (Extended DMS). По требованиям
        // проекта эта программа больше не должна отображаться. Сравниваем по имени
        // без учёта регистра. Если в базе есть программы с похожим названием,
        // можно добавить дополнительные условия (например, код программы).
        programs = programs.stream()
                .filter(p -> {
                    String name = p.getName();
                    return name == null || !name.equalsIgnoreCase("Расширенный ДМС");
                })
                .toList();

        // фильтр по активности
        if (activeFilter != null && !activeFilter.equalsIgnoreCase("all")) {
            boolean activeFlag = activeFilter.equalsIgnoreCase("active");
            programs = programs.stream()
                    .filter(p -> p.isActive() == activeFlag)
                    .toList();
            model.addAttribute("activeFilter", activeFilter);
        }

        // фильтр по региону (FR-02)
        if (regionId != null) {
            programs = programs.stream()
                    .filter(p -> p.getRegions() != null && p.getRegions().stream().anyMatch(r -> r.getId().equals(regionId)))
                    .toList();
            model.addAttribute("regionId", regionId);
        }

        // фильтр по покрытию (FR-02)
        if (coverageId != null) {
            programs = programs.stream()
                    .filter(p -> p.getCoverages() != null && p.getCoverages().stream().anyMatch(c -> c.getId().equals(coverageId)))
                    .toList();
            model.addAttribute("coverageId", coverageId);
        }

        // фильтр по наличию акций (FR-02: promo=yes => только программы с активными акциями)
        if (promo != null && !promo.isBlank()) {
            boolean requirePromo = promo.equalsIgnoreCase("yes");
            programs = programs.stream()
                    .filter(p -> {
                        if (!requirePromo) return true;
                        // есть ли хотя бы одна активная акция
                        return p.getPromoOffers() != null && p.getPromoOffers().stream().anyMatch(po -> {
                            // Проверяем, что акция активна и текущая дата в диапазоне действия
                            if (!po.isActive()) return false;
                            java.time.LocalDate now = java.time.LocalDate.now();
                            return (po.getValidFrom() == null || !now.isBefore(po.getValidFrom())) &&
                                   (po.getValidTo() == null || !now.isAfter(po.getValidTo()));
                        });
                    })
                    .toList();
            model.addAttribute("promo", promo);
        }

        // передаём справочники для фильтров в модель
        model.addAttribute("allRegions", regionService.findAll(null, Sort.by("name")));
        model.addAttribute("allCoverages", coverageService.findAll(null, Sort.by("name")));

        // Пагинация (FR‑02): делим список на страницы по 10 элементов
        int pageSize = 10;
        int currentPage = (page == null || page < 1) ? 1 : page;
        int totalItems = programs.size();
        int totalPages = (int) Math.ceil((double) totalItems / pageSize);
        if (currentPage > totalPages && totalPages > 0) {
            currentPage = totalPages;
        }
        int fromIndex = (currentPage - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, totalItems);
        List<InsuranceProgram> pageContent = programs.subList(fromIndex, toIndex);

        model.addAttribute("programs", pageContent);
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reverseSortDir", "asc".equalsIgnoreCase(sortDir) ? "desc" : "asc");
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalItems", totalItems);
        // передаём текущие фильтры для построения ссылок на другие страницы
        model.addAttribute("keyword", keyword);
        model.addAttribute("activeFilter", activeFilter);
        model.addAttribute("regionId", regionId);
        model.addAttribute("coverageId", coverageId);
        model.addAttribute("promo", promo);

        return "programs/list";
    }

    // КАРТОЧКА ПРОГРАММЫ (read-only)
    @GetMapping("/{id}")
    public String viewProgram(@PathVariable Long id, Model model) {
        InsuranceProgram program = insuranceProgramRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Программа не найдена: " + id));
        model.addAttribute("program", program);
        return "programs/view";
    }

    // ФОРМА ДОБАВЛЕНИЯ
    @GetMapping("/new")
    public String newProgramForm(Model model) {
        model.addAttribute("program", new InsuranceProgram());
        model.addAttribute("formTitle", "Добавление страховой программы");
        // Подгружаем справочники для чекбоксов (FR‑04–FR‑07)
        loadDictionaries(model);
        return "programs/form";
    }

    // ФОРМА РЕДАКТИРОВАНИЯ
    @GetMapping("/{id}/edit")
    public String editProgramForm(@PathVariable Long id, Model model) {
        InsuranceProgram program = insuranceProgramRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Программа не найдена: " + id));
        model.addAttribute("program", program);
        model.addAttribute("formTitle", "Редактирование страховой программы");
        // Подгружаем справочники для чекбоксов
        loadDictionaries(model);
        return "programs/form";
    }

    /**
     * Добавляет в модель списки всех доступных покрытий, исключений, тарифных планов,
     * акций, регионов и медорганизаций для отображения в форме (FR‑04–FR‑07).
     */
    private void loadDictionaries(Model model) {
        // Сортируем по имени по возрастанию
        Sort sortByName = Sort.by(Sort.Direction.ASC, "name");
        model.addAttribute("allCoverages", coverageService.findAll(null, sortByName));
        model.addAttribute("allExclusions", exclusionService.findAll(null, sortByName));
        model.addAttribute("allPromoOffers", promoOfferService.findAll(null, sortByName));
        model.addAttribute("allRegions", regionService.findAll(null, sortByName));
        model.addAttribute("allMedicalOrgs", medicalOrganizationService.findAll(null, sortByName));
    }

    @PostMapping("/save")
    public String saveProgram(@ModelAttribute("program") InsuranceProgram program) {
        insuranceProgramRepository.save(program);
        return "redirect:/programs";
    }

    @PostMapping("/{id}/delete")
    public String deleteProgram(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            insuranceProgramRepository.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            // Если программа связана с заявками, полисами или другими сущностями,
            // удаление приведёт к ошибке целостности. Показываем сообщение пользователю.
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Нельзя удалить программу, так как существуют связанные заявки или полисы.");
        }
        return "redirect:/programs";
    }
}
