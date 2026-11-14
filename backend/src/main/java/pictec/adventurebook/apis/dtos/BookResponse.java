package pictec.adventurebook.apis.dtos;

import java.time.Instant;

import pictec.adventurebook.domain.entities.BookMetadata;
import pictec.adventurebook.domain.enums.BookDifficulty;

public record BookResponse(String id, String title, String author, String description,
        String category, BookDifficulty difficulty, Instant createdAt) {

    public static BookResponse of(BookMetadata b) {
        return new BookResponse(b.getId(), b.getTitle(), b.getAuthor(), b.getDescription(),
                b.getCategory(), b.getDifficulty(), b.getCreatedAt());
    }
}