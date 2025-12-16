package ru.fa.dpi23.dmsisms.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.fa.dpi23.dmsisms.entity.PromoOffer;
import ru.fa.dpi23.dmsisms.service.PromoOfferService;
import ru.fa.dpi23.dmsisms.service.InsuranceProgramService;

import java.util.List;

@Controller
@RequestMapping("/promo-offers")
@RequiredArgsConstructor
public class PromoOfferController {

    private final PromoOfferService promoOfferService;
    private final InsuranceProgramService insuranceProgramService;

    @GetMapping
    public String list(@RequestParam(value = "keyword", required = false) String keyword,
                       Model model) {

        Sort sort = Sort.by(Sort.Direction.ASC, "name");
        List<PromoOffer> items = promoOfferService.findAll(keyword, sort);

        model.addAttribute("title", "Акции и скидки");
        model.addAttribute("items", items);
        model.addAttribute("keyword", keyword);

        return "refs/promo-offers";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("title", "Новая акция / скидка");
        model.addAttribute("item", new PromoOffer());
        // Load programs and tariffs for multi-select
        model.addAttribute("allPrograms", insuranceProgramService.list(null, "name", "asc"));
        return "refs/promo-offers-form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        PromoOffer offer = promoOfferService.findById(id);
        model.addAttribute("title", "Редактирование акции");
        model.addAttribute("item", offer);
        // Load programs and tariffs for multi-select
        model.addAttribute("allPrograms", insuranceProgramService.list(null, "name", "asc"));
        return "refs/promo-offers-form";
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("item") PromoOffer offer,
                       BindingResult bindingResult,
                       Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("title",
                    offer.getId() == null ? "Новая акция / скидка" : "Редактирование акции");
            // Re-populate lists if validation fails
            model.addAttribute("allPrograms", insuranceProgramService.list(null, "name", "asc"));
            return "refs/promo-offers-form";
        }

        promoOfferService.save(offer);
        return "redirect:/promo-offers";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        // Перед удалением проверяем, можно ли удалить (нет привязок)
        if (!promoOfferService.canDeletePromoOffer(id)) {
            return "redirect:/promo-offers?error=Нельзя удалить акцию, так как она используется в продуктах или тарифах.";
        }
        promoOfferService.deleteById(id);
        return "redirect:/promo-offers";
    }
}