package pictec.adventurebook.services;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.*;
import static pictec.adventurebook.support.BookFixtures.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import pictec.adventurebook.AdventureBookApplication;
import pictec.adventurebook.domain.enums.BookDifficulty;
import pictec.adventurebook.domain.enums.GameStatus;
import pictec.adventurebook.domain.exceptions.ConflictException;
import pictec.adventurebook.services.irespositories.IGameSessionRepository;

@SpringBootTest(classes = AdventureBookApplication.class)
class GameFlowTest {

    private static final Path STORAGE = createTempDir();

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", () -> "jdbc:h2:mem:flowtest;DB_CLOSE_DELAY=-1");
        r.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        r.add("books.storage.dir", STORAGE::toString);
        r.add("books.initial-health", () -> "10");
    }

    @Autowired
    BookApplicationService books;
    @Autowired
    GameApplicationService games;
    @Autowired
    IGameSessionRepository sessionRepo;

    private String newBook() {
        return books.insert(VALID_JSON.getBytes(UTF_8), metadata(BookDifficulty.EASY)).getId();
    }

    @Test
    void choice_isPersistedAndGameCanBeReloaded() {
        var bookId = newBook();
        var started = games.startGame("u1", bookId);
        var sessionId = started.gameSession().getId();

        assertEquals(sessionId, games.resumeGameInProgress("u1", bookId).orElseThrow().gameSession().getId());

        games.chooseOption("u1", sessionId, 0);

        var reloaded = games.getGame("u1", sessionId);
        assertEquals("s2", reloaded.section().id());
        assertEquals(GameStatus.FINISHED, reloaded.gameSession().getStatus());
        assertTrue(games.resumeGameInProgress("u1", bookId).isEmpty());
    }

    @Test
    void restart_abandonsInProgressSession() {
        var bookId = newBook();
        var first = games.startGame("u1", bookId).gameSession().getId();

        var second = games.restartGame("u1", first).gameSession().getId();

        assertEquals(GameStatus.ABANDONED, sessionRepo.findById(first).orElseThrow().getStatus());
        assertEquals(GameStatus.IN_PROGRESS, sessionRepo.findById(second).orElseThrow().getStatus());
    }

    @Test
    void list_filtersAndDeleteRules() {
        var bookId = newBook();

        assertTrue(books.list(null, null, null).stream().anyMatch(b -> b.getId().equals(bookId)));
        assertTrue(books.list("t", null, BookDifficulty.EASY).stream().anyMatch(b -> b.getId().equals(bookId)));
        assertTrue(books.list(null, null, BookDifficulty.HARD).stream().noneMatch(b -> b.getId().equals(bookId)));

        games.startGame("u1", bookId);
        assertThrows(ConflictException.class, () -> books.delete(bookId));
    }

    private static Path createTempDir() {
        try {
            return Files.createTempDirectory("books-test");
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}