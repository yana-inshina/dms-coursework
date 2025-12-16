package ru.fa.dpi23.dmsisms.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

    @GetMapping("/")
    public String home() {
        // после успешного логина Spring Security отправит сюда
        return "home"; // resources/templates/home.html
    }
}