package kz.iitu.springlab.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api")
public class HelloController {

    @Value("${app.owner:unknown}")
    private String owner;

    @GetMapping("/hello")
    public Greeting hello(@RequestParam(defaultValue = "world") String name) {
        return new Greeting("Hello, " + name + "!", owner, LocalDateTime.now());
    }

    @GetMapping("/info")
    public Info info() {
        return new Info(owner, System.getProperty("java.version"), Runtime.getRuntime().availableProcessors());
    }

    // Variant 5: Fahrenheit -> Celsius and Kelvin
    @GetMapping("/temperature")
    public Temperature temperature(@RequestParam(defaultValue = "32") double f) {
        double c = (f - 32) * 5 / 9;
        double k = c + 273.15;
        return new Temperature(f, Math.round(c * 100) / 100.0, Math.round(k * 100) / 100.0);
    }

    public record Greeting(String message, String owner, LocalDateTime timestamp) { }

    public record Info(String owner, String javaVersion, int cpuCores) { }

    public record Temperature(double fahrenheit, double celsius, double kelvin) { }
}
