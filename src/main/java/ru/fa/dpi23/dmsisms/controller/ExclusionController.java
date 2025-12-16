package ru.fa.dpi23.dmsisms.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.fa.dpi23.dmsisms.entity.Exclusion;
import ru.fa.dpi23.dmsisms.service.ExclusionService;

import jakarta.validation.Valid;
import java.util.List;

@Controller
@RequestMapping("/exclusions")
@RequiredArgsConstructor
public class ExclusionController {

    private final ExclusionService exclusionService;

    @GetMapping
    public String list(@RequestParam(value = "keyword", required = false) String keyword,
                       Model model) {
        Sort sort = Sort.by(Sort.Direction.ASC, "name");
        List<Exclusion> items = exclusionService.findAll(keyword, sort);

        model.addAttribute("title", "Исключения");
        model.addAttribute("items", items);
        model.addAttribute("keyword", keyword);

        return "refs/exclusions"; // путь к шаблону списка
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("title", "Новое исключение");
        model.addAttribute("item", new Exclusion());
        return "refs/exclusions-form"; // путь к шаблону формы
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Exclusion exclusion = exclusionService.findById(id);
        model.addAttribute("title", "Редактирование исключения");
        model.addAttribute("item", exclusion);
        return "refs/exclusions-form";
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("item") Exclusion exclusion,
                       BindingResult bindingResult,
                       Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("title",
                    exclusion.getId() == null ? "Новое исключение" : "Редактирование исключения");
            return "refs/exclusions-form";
        }

        exclusionService.save(exclusion);
        return "redirect:/exclusions";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        try {
            exclusionService.deleteById(id);
        } catch (IllegalStateException ex) {
            return "redirect:/exclusions?error=" + ex.getMessage();
        }
        return "redirect:/exclusions";
    }
}