package kz.iitu.springlab.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import kz.iitu.springlab.security.UserAccountService;
import kz.iitu.springlab.security.UserAccountService.UserAccount;
import kz.iitu.springlab.security.UserAccountService.UsernameTakenException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.stream.Collectors;

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

    // Variant 4: a new user gets an Argon2 hash, the old ones keep their BCrypt hashes
    @PostMapping("/users")
    public String create(@Valid NewUserForm form, BindingResult errors, RedirectAttributes redirectAttributes) {
        if (errors.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "The user was not created: " + errors.getFieldErrors().stream()
                    .map(e -> e.getField() + " " + e.getDefaultMessage())
                    .collect(Collectors.joining("; ")));
            return "redirect:/admin";
        }
        try {
            UserAccount created = accounts.create(form.username(), form.password(), Boolean.TRUE.equals(form.administrator()));
            redirectAttributes.addFlashAttribute("message",
                    "User «" + created.username() + "» created; password stored as " + created.storedAs());
        } catch (UsernameTakenException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin";
    }

    public record NewUserForm(
            @NotBlank @Pattern(regexp = "[A-Za-z0-9._-]{3,60}", message = "must be 3-60 letters, digits, '.', '_' or '-'")
            String username,
            @NotBlank @Size(min = 6, max = 100) String password,
            Boolean administrator) {   // an unchecked checkbox sends nothing: null, not false

    }
}
