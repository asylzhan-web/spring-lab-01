package kz.iitu.springlab.catalog;

import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class BookRepository {

    private final Map<Long, Book> books = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong();

    public BookRepository() {
        save(new Book(null, "Effective Java", "Joshua Bloch", 2018));
        save(new Book(null, "Clean Code", "Robert Martin", 2008));
        save(new Book(null, "Spring in Action", "Craig Walls", 2022));
    }

    public List<Book> findAll() {
        return List.copyOf(books.values().stream().sorted(Comparator.comparing(Book::id)).toList());
    }

    public Optional<Book> findById(long id) {
        return Optional.ofNullable(books.get(id));
    }

    public Book save(Book book) {
        Book stored = book.id() == null
                ? new Book(sequence.incrementAndGet(), book.title(), book.author(), book.year())
                : book;
        books.put(stored.id(), stored);
        return stored;
    }

    public boolean deleteById(long id) {
        return books.remove(id) != null;
    }
}
