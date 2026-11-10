package kz.iitu.springlab.web;

import kz.iitu.springlab.security.UserAccountService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

// /admin/** is ADMIN only (SecurityConfiguration); the service checks the role again (@PreAuthorize)
@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserAccountService accounts;

    public AdminController(UserAccountService accounts) {
        this.accounts = accounts;
    }

    @GetMapping
    public String users(Model model) {
        model.addAttribute("users", accounts.findAll());
        model.addAttribute("title", "Administration");
        return "admin/users";
    }
}
