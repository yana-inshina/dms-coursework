package ru.fa.dpi23.dmsisms.controller;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.fa.dpi23.dmsisms.entity.*;
import ru.fa.dpi23.dmsisms.repository.RoleRepository;
import ru.fa.dpi23.dmsisms.repository.UserRepository;

import java.security.Principal;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @GetMapping("/users")
    public String users(Model model) {
        model.addAttribute("users", userRepository.findAll());
        model.addAttribute("roles", RoleName.values());
        return "admin/users";
    }

    @PostMapping("/users/{id}/role")
    @Transactional
    public String updateRole(@PathVariable Long id,
                             @RequestParam("role") RoleName roleName,
                             Principal principal,
                             RedirectAttributes ra) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден: " + id));

        // защита от самоблокировки: чтобы админ не снял ADMIN с самого себя
        if (principal != null && principal.getName().equals(user.getUsername()) && roleName != RoleName.ADMIN) {
            ra.addFlashAttribute("error", "Нельзя снять роль ADMIN у самого себя.");
            return "redirect:/admin/users";
        }

        Role role = roleRepository.findByName(roleName)
                .orElseGet(() -> roleRepository.save(new Role(roleName)));

        // делаем одну “главную” роль: очищаем старые и ставим новую
        user.getUserRoles().clear();

        UserRole ur = new UserRole();
        ur.setUser(user);
        ur.setRole(role);
        user.getUserRoles().add(ur);

        userRepository.save(user);

        ra.addFlashAttribute("success", "Роль пользователя обновлена: " + user.getUsername());
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/enabled")
    public String setEnabled(@PathVariable Long id,
                             @RequestParam("enabled") boolean enabled,
                             Principal principal,
                             RedirectAttributes ra) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден: " + id));

        // чтобы админ сам себя случайно не выключил
        if (principal != null && principal.getName().equals(user.getUsername()) && !enabled) {
            ra.addFlashAttribute("error", "Нельзя отключить самого себя.");
            return "redirect:/admin/users";
        }

        user.setEnabled(enabled);
        userRepository.save(user);

        ra.addFlashAttribute("success", "Статус пользователя обновлен: " + user.getUsername());
        return "redirect:/admin/users";
    }
}
