package kz.iitu.springlab.catalog;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "book")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 120)
    private String author;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Genre genre;

    @Column(name = "published_year", nullable = false)
    private int year;

    protected Book() {
        // for JPA
    }

    public Book(String title, String author, Genre genre, int year) {
        update(title, author, genre, year);
    }

    void update(String title, String author, Genre genre, int year) {
        this.title = title;
        this.author = author;
        this.genre = genre;
        this.year = year;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public Genre getGenre() {
        return genre;
    }

    public int getYear() {
        return year;
    }

    @Override
    public String toString() {
        return "Book[id=" + id + ", title=" + title + ", author=" + author + ", genre=" + genre + ", year=" + year + "]";
    }
}
