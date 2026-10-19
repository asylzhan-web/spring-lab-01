package kz.iitu.springlab.service;

import kz.iitu.springlab.audit.Audited;
import kz.iitu.springlab.audit.Sensitive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CatalogService {

    // Self-injection: calls through "self" go through the proxy, so the aspects work
    @Autowired
    @Lazy
    private CatalogService self;

    public String findById(long id) {
        pause(50);
        return "Item no. " + id;
    }

    @Audited(action = "CATALOG_LIST", logArguments = true)
    public List<String> findAll(int limit) {
        pause(300);
        List<String> items = new ArrayList<>();
        for (int i = 1; i <= limit; i++) {
            items.add("Item no. " + i);
        }
        return items;
    }

    @Audited(action = "CATALOG_REMOVE")
    public String remove(long id) {
        if (id <= 0) {
            throw new IllegalArgumentException("Invalid identifier: " + id);
        }
        return "Removed item no. " + id;
    }

    // Self-invocation demo: "this.remove" bypasses the proxy
    public List<String> removeTwice(long id) {
        return List.of(remove(id), this.remove(id + 1));
    }

    // Fixed version: calls go through the proxy via the self-injected reference
    public List<String> removeTwiceViaProxy(long id) {
        return List.of(self.remove(id), self.remove(id + 1));
    }

    // Variant 4: arguments marked @Sensitive are masked in the log
    public String login(String user, @Sensitive String password) {
        return "User " + user + " logged in (password length " + password.length() + ")";
    }

    private static void pause(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
