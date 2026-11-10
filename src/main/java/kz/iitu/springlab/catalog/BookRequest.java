package kz.iitu.springlab.catalog;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// What a client may send to create or replace a book: the entity itself is never bound to a request
public record BookRequest(
        @NotBlank @Size(max = 200) String title,
        @Size(max = 120) String author,
        @NotNull Genre genre,
        @Min(1) @Max(2100) int year) {
}
