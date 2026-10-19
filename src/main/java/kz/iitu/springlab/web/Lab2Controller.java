package kz.iitu.springlab.web;

import kz.iitu.springlab.lifecycle.LifecycleDemo;
import kz.iitu.springlab.notify.NotificationService;
import kz.iitu.springlab.scope.TicketOffice;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/lab2")
public class Lab2Controller {

    private final NotificationService service;
    private final LifecycleDemo lifecycle;
    private final TicketOffice office;

    public Lab2Controller(NotificationService service, LifecycleDemo lifecycle, TicketOffice office) {
        this.service = service;
        this.lifecycle = lifecycle;
        this.office = office;
    }

    @GetMapping("/notify")
    public Map<String, Object> notify(@RequestParam(defaultValue = "Hello") String text) {
        return Map.of(
                "primary", service.viaPrimary(text),
                "console", service.viaConsole(text),
                "all", service.viaAll(text),
                "beanNames", service.names());
    }

    @GetMapping("/lifecycle")
    public List<String> lifecycle() {
        return lifecycle.events();
    }

    @GetMapping("/scopes")
    public Map<String, Object> scopes() {
        return office.demo();
    }

    // Variant 5: calls exactly the "truncating" notifier
    @GetMapping("/custom")
    public Map<String, Object> custom(@RequestParam(defaultValue = "Hello") String text) {
        return Map.of("input", text, "length", text.length(), "result", service.viaTruncating(text));
    }
}
