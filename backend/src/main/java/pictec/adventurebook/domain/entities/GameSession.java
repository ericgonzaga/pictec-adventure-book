package pictec.adventurebook.domain.entities;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pictec.adventurebook.domain.enums.GameStatus;

@Entity
@Table(name = "game_sessions")
@NoArgsConstructor
@Getter
@Setter
public class GameSession {

    @Id
    private String id = UUID.randomUUID().toString();

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String bookId;

    @Column(nullable = false)
    private String currentBookSectionId;

    private int health;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GameStatus status;

    @Column(nullable = false)
    private Instant updatedAt;

    @Version
    private long version; // optimistic-lock

    public GameSession(String userId, String bookId, String currentBookSectionId, int health) {
        this.userId = userId;
        this.bookId = bookId;
        this.currentBookSectionId = currentBookSectionId;
        this.health = health;
        this.status = GameStatus.IN_PROGRESS;
        this.updatedAt = Instant.now();
    }

    public void moveTo(String nextBookSectionId, int health, GameStatus status) {
        this.currentBookSectionId = nextBookSectionId;
        this.health = health;
        this.status = status;
        this.updatedAt = Instant.now();
    }

    public boolean isInProgress() {
        return status == GameStatus.IN_PROGRESS;
    }
}
