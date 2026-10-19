package kz.iitu.springlab.notify;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

// Variant 5: truncates messages longer than 20 characters and appends "..."
@Component("truncating")
@Order(3)
public class TruncatingNotifier implements Notifier {

    private static final Logger log = LoggerFactory.getLogger(TruncatingNotifier.class);
    private static final int MAX_LENGTH = 20;

    @PostConstruct
    void init() {
        log.info("TRUNCATING >> initialised, max length = {}", MAX_LENGTH);
    }

    @Override
    public String send(String m) {
        String result = m.length() > MAX_LENGTH ? m.substring(0, MAX_LENGTH) + "..." : m;
        log.info("TRUNCATING >> {}", result);
        return "truncating: " + result;
    }

    @Override
    public String channel() {
        return "truncating";
    }
}
