package ru.fa.dpi23.dmsisms.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/menu")
    public String home() {
        return "home";
    }
}
