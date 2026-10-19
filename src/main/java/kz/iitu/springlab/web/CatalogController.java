package kz.iitu.springlab.web;

import kz.iitu.springlab.service.CatalogService;
import org.springframework.aop.support.AopUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/lab4")
public class CatalogController {

    private final CatalogService catalog;

    public CatalogController(CatalogService catalog) {
        this.catalog = catalog;
    }

    @GetMapping("/item/{id}")
    public String item(@PathVariable long id) {
        return catalog.findById(id);
    }

    @GetMapping("/items")
    public List<String> items(@RequestParam(defaultValue = "5") int limit) {
        return catalog.findAll(limit);
    }

    @DeleteMapping("/item/{id}")
    public String remove(@PathVariable long id) {
        return catalog.remove(id);
    }

    @GetMapping("/proxy")
    public Map<String, Object> proxy() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("className", catalog.getClass().getName());
        result.put("superClass", catalog.getClass().getSuperclass().getName());
        result.put("isAopProxy", AopUtils.isAopProxy(catalog));
        result.put("isCglib", AopUtils.isCglibProxy(catalog));
        return result;
    }

    @GetMapping("/remove-twice/{id}")
    public List<String> removeTwice(@PathVariable long id) {
        return catalog.removeTwice(id);
    }

    @GetMapping("/remove-twice-fixed/{id}")
    public List<String> removeTwiceFixed(@PathVariable long id) {
        return catalog.removeTwiceViaProxy(id);
    }

    // Variant 4 demo: the password is a @Sensitive parameter
    @PostMapping("/login")
    public String login(@RequestParam String user, @RequestParam String password) {
        return catalog.login(user, password);
    }
}
