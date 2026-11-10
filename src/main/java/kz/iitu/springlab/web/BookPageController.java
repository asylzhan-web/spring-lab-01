package kz.iitu.springlab.web;

import jakarta.validation.Valid;
import kz.iitu.springlab.catalog.Book;
import kz.iitu.springlab.catalog.BookRequest;
import kz.iitu.springlab.catalog.BookService;
import kz.iitu.springlab.catalog.Genre;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.stream.Collectors;

// GET /books/** is open to USER and ADMIN, every other method on /books/** to ADMIN only (SecurityConfiguration)
@Controller
@RequestMapping("/books")
public class BookPageController {

    private final BookService service;

    public BookPageController(BookService service) {
        this.service = service;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String author, Model model) {
        model.addAttribute("books", service.findAll(author));
        model.addAttribute("author", author);
        model.addAttribute("genres", Genre.values());
        model.addAttribute("title", "Catalogue");
        return "books/list";
    }

    @PostMapping
    public String create(@Valid BookRequest form, BindingResult errors, RedirectAttributes redirectAttributes) {
        if (errors.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "The book was not added: " + describe(errors));
            return "redirect:/books";
        }
        Book created = service.create(form);
        redirectAttributes.addFlashAttribute("message", "Book «" + created.getTitle() + "» has been added");
        return "redirect:/books";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable long id, RedirectAttributes redirectAttributes) {
        if (service.delete(id)) {
            redirectAttributes.addFlashAttribute("message", "Book no. " + id + " has been deleted");
        } else {
            redirectAttributes.addFlashAttribute("error", "Book no. " + id + " does not exist");
        }
        return "redirect:/books";
    }

    private static String describe(BindingResult errors) {
        return errors.getFieldErrors().stream()
                .map(e -> e.getField() + " " + (e.isBindingFailure() ? "has an invalid value" : e.getDefaultMessage()))
                .collect(Collectors.joining("; "));
    }
}
