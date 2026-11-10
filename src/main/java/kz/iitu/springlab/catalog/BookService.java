package kz.iitu.springlab.catalog;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class BookService {

    private static final Sort BY_ID = Sort.by("id");

    private final BookRepository repository;

    public BookService(BookRepository repository) {
        this.repository = repository;
    }

    // author == null -> all books; otherwise case-insensitive "contains" (so "Bloch" matches "Joshua Bloch")
    public List<Book> findAll(String author) {
        if (author == null || author.isBlank()) {
            return repository.findAll(BY_ID);
        }
        return repository.findByAuthorContainingIgnoreCaseOrderByIdAsc(author.strip());
    }

    // Lab 5, variant 4: paging
    public Page<Book> findPage(int page, int size) {
        return repository.findAll(PageRequest.of(page, size, BY_ID));
    }

    public Optional<Book> findById(long id) {
        return repository.findById(id);
    }

    // Lab 10, task 3.2: the rule travels with the method. Whatever controller calls it, only ADMIN gets through.
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public Book create(BookRequest request) {
        return repository.save(new Book(request.title(), blankToNull(request.author()), request.genre(), request.year()));
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public Optional<Book> replace(long id, BookRequest request) {
        return repository.findById(id).map(book -> {
            book.update(request.title(), blankToNull(request.author()), request.genre(), request.year());
            return book;
        });
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public boolean delete(long id) {
        if (!repository.existsById(id)) {
            return false;
        }
        repository.deleteById(id);
        return true;
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }
}
