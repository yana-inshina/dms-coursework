package ru.fa.dpi23.dmsisms.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.fa.dpi23.dmsisms.entity.Client;
import ru.fa.dpi23.dmsisms.service.ClientService;

@Controller
@org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
@RequestMapping("/clients")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;

    @GetMapping
    public String listClients(@RequestParam(value = "keyword", required = false) String keyword,
                              @RequestParam(value = "sortField", defaultValue = "fullName") String sortField,
                              @RequestParam(value = "sortDir", defaultValue = "asc") String sortDir,
                              Model model) {

        model.addAttribute("clients", clientService.list(keyword, sortField, sortDir));
        model.addAttribute("keyword", keyword);
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reverseSortDir", "asc".equalsIgnoreCase(sortDir) ? "desc" : "asc");

        return "clients/list";
    }

    @GetMapping("/new")
    public String newClientForm(Model model) {
        model.addAttribute("client", new Client());
        model.addAttribute("formTitle", "Добавление клиента");
        return "clients/form";
    }

    @GetMapping("/{id}/edit")
    public String editClientForm(@PathVariable Long id, Model model) {
        model.addAttribute("client", clientService.get(id));
        model.addAttribute("formTitle", "Редактирование клиента");
        return "clients/form";
    }

    @PostMapping("/save")
    public String saveClient(@ModelAttribute("client") Client client) {
        clientService.save(client);
        return "redirect:/clients";
    }

    @PostMapping("/{id}/delete")
    public String deleteClient(@PathVariable Long id) {
        clientService.delete(id);
        return "redirect:/clients";
    }
}
