package ru.fa.dpi23.dmsisms.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.fa.dpi23.dmsisms.entity.Coverage;
import ru.fa.dpi23.dmsisms.service.CoverageService;

import java.util.List;

@Controller
@RequestMapping("/coverages")
@RequiredArgsConstructor
public class CoverageController {

    private final CoverageService coverageService;

    @GetMapping
    public String list(@RequestParam(value = "keyword", required = false) String keyword,
                       Model model) {
        Sort sort = Sort.by("name").ascending();
        List<Coverage> coverages = coverageService.findAll(keyword, sort);
        if (keyword != null && !keyword.isBlank()) {
            model.addAttribute("keyword", keyword);
        }
        model.addAttribute("items", coverages);
        model.addAttribute("title", "Покрываемые услуги и заболевания");
        model.addAttribute("listPath", "/coverages");
        return "refs/coverage-list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("item", new Coverage());
        model.addAttribute("title", "Добавление покрытия");
        model.addAttribute("listPath", "/coverages");
        return "refs/coverage-form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Coverage coverage = coverageService.findById(id);
        model.addAttribute("item", coverage);
        model.addAttribute("title", "Редактирование покрытия");
        model.addAttribute("listPath", "/coverages");
        return "refs/coverage-form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute("item") Coverage coverage) {
        coverageService.save(coverage);
        return "redirect:/coverages";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        try {
            coverageService.deleteById(id);
        } catch (IllegalStateException ex) {
            // Если покрытие привязано, перенаправляем обратно на список с сообщением об ошибке
            return "redirect:/coverages?error=" + ex.getMessage();
        }
        return "redirect:/coverages";
    }
}
