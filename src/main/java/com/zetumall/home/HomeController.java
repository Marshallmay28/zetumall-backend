package com.zetumall.home;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        // Redirect root access to Swagger UI for better developer experience
        return "redirect:/swagger-ui.html";
    }

    @GetMapping("/api")
    public String apiRoot() {
        return "redirect:/swagger-ui.html";
    }
}
