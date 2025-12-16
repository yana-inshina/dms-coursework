package ru.fa.dpi23.dmsisms.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.fa.dpi23.dmsisms.entity.MedicalOrganization;
import ru.fa.dpi23.dmsisms.service.MedicalOrganizationService;
import ru.fa.dpi23.dmsisms.service.RegionService;

import java.util.List;

@Controller
@RequestMapping("/medical-orgs")
@RequiredArgsConstructor
public class MedicalOrganizationController {

    private final MedicalOrganizationService medicalOrganizationService;
    private final RegionService regionService;

    @GetMapping
    public String list(@RequestParam(value = "keyword", required = false) String keyword,
                       Model model) {

        Sort sort = Sort.by(Sort.Direction.ASC, "name");
        List<MedicalOrganization> items =
                medicalOrganizationService.findAll(keyword, sort);

        model.addAttribute("title", "Медорганизации");
        model.addAttribute("items", items);
        model.addAttribute("keyword", keyword);

        return "refs/medical-orgs";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("title", "Новая медорганизация");
        model.addAttribute("item", new MedicalOrganization());
        model.addAttribute("allRegions", regionService.findAllSortedByName());
        return "refs/medical-orgs-form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        MedicalOrganization org = medicalOrganizationService.findById(id);
        model.addAttribute("title", "Редактирование медорганизации");
        model.addAttribute("item", org);
        model.addAttribute("allRegions", regionService.findAllSortedByName());
        return "refs/medical-orgs-form";
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("item") MedicalOrganization org,
                       BindingResult bindingResult,
                       Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("title",
                    org.getId() == null ? "Новая медорганизация" : "Редактирование медорганизации");
            model.addAttribute("allRegions", regionService.findAllSortedByName());
            return "refs/medical-orgs-form";
        }

        try {
            medicalOrganizationService.save(org);
        } catch (IllegalStateException ex) {
            bindingResult.rejectValue("region", "error.item", ex.getMessage());
            model.addAttribute("title",
                    org.getId() == null ? "Новая медорганизация" : "Редактирование медорганизации");
            model.addAttribute("allRegions", regionService.findAllSortedByName());
            return "refs/medical-orgs-form";
        }
        return "redirect:/medical-orgs";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        try {
            medicalOrganizationService.deleteById(id);
        } catch (IllegalStateException ex) {
            return "redirect:/medical-orgs?error=" + ex.getMessage();
        }
        return "redirect:/medical-orgs";
    }
}