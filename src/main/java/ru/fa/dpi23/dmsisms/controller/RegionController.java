package ru.fa.dpi23.dmsisms.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.fa.dpi23.dmsisms.entity.Region;
import ru.fa.dpi23.dmsisms.service.RegionService;

import java.util.List;

@Controller
@RequestMapping("/regions")
@RequiredArgsConstructor
public class RegionController {

    private final RegionService regionService;

    @GetMapping
    public String list(@RequestParam(value = "keyword", required = false) String keyword,
                       Model model) {

        Sort sort = Sort.by(Sort.Direction.ASC, "name");
        List<Region> items = regionService.findAll(keyword, sort);

        model.addAttribute("title", "Регионы");
        model.addAttribute("items", items);
        model.addAttribute("keyword", keyword);

        return "refs/regions";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("title", "Новый регион");
        model.addAttribute("item", new Region());
        return "refs/regions-form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Region region = regionService.findById(id);
        model.addAttribute("title", "Редактирование региона");
        model.addAttribute("item", region);
        return "refs/regions-form";
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("item") Region region,
                       BindingResult bindingResult,
                       Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("title",
                    region.getId() == null ? "Новый регион" : "Редактирование региона");
            return "refs/regions-form";
        }

        regionService.save(region);
        return "redirect:/regions";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        regionService.deleteById(id);
        return "redirect:/regions";
    }
}