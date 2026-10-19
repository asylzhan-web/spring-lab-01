package kz.iitu.springlab.notify;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class NotificationService {

    private final Notifier primary;
    private final Notifier console;
    private final Notifier truncating;
    private final List<Notifier> all;
    private final Map<String, Notifier> byName;

    public NotificationService(Notifier primary,
                               @Qualifier("console") Notifier console,
                               @Qualifier("truncating") Notifier truncating,
                               List<Notifier> all,
                               Map<String, Notifier> byName) {
        this.primary = primary;
        this.console = console;
        this.truncating = truncating;
        this.all = all;
        this.byName = byName;
    }

    public String viaPrimary(String text) {
        return primary.send(text);
    }

    public String viaConsole(String text) {
        return console.send(text);
    }

    public String viaTruncating(String text) {
        return truncating.send(text);
    }

    public List<String> viaAll(String text) {
        return all.stream().map(n -> n.send(text)).toList();
    }

    public Set<String> names() {
        return byName.keySet();
    }
}
