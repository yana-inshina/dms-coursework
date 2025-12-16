package ru.fa.dpi23.dmsisms.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.fa.dpi23.dmsisms.entity.Role;
import ru.fa.dpi23.dmsisms.entity.RoleName;
import ru.fa.dpi23.dmsisms.entity.User;
import ru.fa.dpi23.dmsisms.entity.UserRole;
import ru.fa.dpi23.dmsisms.repository.RoleRepository;
import ru.fa.dpi23.dmsisms.repository.UserRepository;
import ru.fa.dpi23.dmsisms.repository.UserRoleRepository;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;

    // Свой шаблон логина
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    // Форма регистрации
    @GetMapping("/register")
    public String showRegisterForm(Model model,
                                   @RequestParam(value = "error", required = false) String error) {
        model.addAttribute("error", error);
        return "register";
    }

    // Обработка регистрации
    @PostMapping("/register")
    public String processRegister(@RequestParam("username") String username,
                                  @RequestParam("password") String password,
                                  @RequestParam("fullName") String fullName,
                                  @RequestParam("email") String email,
                                  @RequestParam(value = "accountType", required = false) String accountType,
                                  Model model) {

        // Проверяем обязательные поля
        if (username == null || username.isBlank() ||
                password == null || password.isBlank() ||
                fullName == null || fullName.isBlank() ||
                email == null || email.isBlank()) {
            model.addAttribute("error", "Логин, пароль, ФИО и email не должны быть пустыми");
            return "register";
        }

        if (userRepository.existsByUsername(username)) {
            model.addAttribute("error", "Пользователь с таким логином уже существует");
            return "register";
        }

        // создаём пользователя
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setFullName(fullName.trim());
        // сохраняем email
        user.setEmail(email.trim());
        user.setEnabled(true);
        user = userRepository.save(user);

        // определяем роль в зависимости от выбранного типа аккаунта
        // используем отдельную переменную, чтобы она была effectively final при использовании внутри лямбды
        final RoleName effectiveType;
        if (accountType != null) {
            RoleName parsed;
            try {
                parsed = RoleName.valueOf(accountType);
            } catch (IllegalArgumentException e) {
                // если передан неизвестный тип, по умолчанию INDIVIDUAL
                parsed = RoleName.INDIVIDUAL;
            }
            effectiveType = parsed;
        } else {
            effectiveType = RoleName.INDIVIDUAL;
        }

        Role defaultRole = roleRepository.findByName(effectiveType)
                .orElseThrow(() -> new IllegalStateException("Роль " + effectiveType + " не найдена в БД"));

        UserRole link = UserRole.builder()
                .user(user)
                .role(defaultRole)
                .build();

        userRoleRepository.save(link);

        // после успешной регистрации — на логин
        return "redirect:/login?registered";
    }
}
