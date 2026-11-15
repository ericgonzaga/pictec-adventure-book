package pictec.adventurebook.domain.entities;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pictec.adventurebook.domain.enums.BookDifficulty;

@Entity
@Table(name = "books")
@NoArgsConstructor
@Getter
@Setter
public class BookMetadata {

    public BookMetadata(String id, String title, String author, String description,
            String category, BookDifficulty difficulty, String key) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.description = description;
        this.category = category;
        this.difficulty = difficulty;
        this.contentKey = key;
        this.createdAt = Instant.now();
    }

    @Id
    private String id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String author;

    @Size(max = 500)
    private String description;

    /**
     * TODO: create a new entity category managed by the user.
     * We can also change this field to a list to allow multiple categories.
     */
    @Size(max = 100)
    private String category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull
    private BookDifficulty difficulty;

    @Column(nullable = false, unique = true)
    private String contentKey;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();
}