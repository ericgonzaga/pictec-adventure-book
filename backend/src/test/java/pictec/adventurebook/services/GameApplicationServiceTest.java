package pictec.adventurebook.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static pictec.adventurebook.support.BookFixtures.content;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pictec.adventurebook.domain.entities.GameSession;
import pictec.adventurebook.domain.enums.GameStatus;
import pictec.adventurebook.domain.exceptions.ConflictException;
import pictec.adventurebook.domain.exceptions.GameFinishedException;
import pictec.adventurebook.domain.exceptions.NotFoundException;
import pictec.adventurebook.domain.logic.GameEngine;
import pictec.adventurebook.services.irepositories.IGameSessionRepository;

@ExtendWith(MockitoExtension.class)
class GameApplicationServiceTest {

    @Mock
    BookApplicationService books;
    @Mock
    IGameSessionRepository sessions;

    GameApplicationService service;

    @BeforeEach
    void setUp() {
        service = new GameApplicationService(books, sessions, new GameEngine(10));
    }

    private GameSession existing(String userId) {
        var s = new GameSession(userId, "b1", "s1", 10);
        lenient().when(sessions.findById(s.getId())).thenReturn(Optional.of(s));
        return s;
    }

    @Test
    void startGame_savesSessionAtBeginWithInitialHealth() {
        when(books.getContent("b1")).thenReturn(content());
        when(sessions.save(any())).thenAnswer(i -> i.getArgument(0));
        when(books.getSection("b1", "s1")).thenReturn(content().sections().get(0));

        var state = service.startGame("u1", "b1");

        assertEquals("s1", state.gameSession().getCurrentBookSectionId());
        assertEquals(10, state.gameSession().getHealth());
        assertTrue(state.gameSession().isInProgress());
        assertEquals("s1", state.section().id());
        verify(sessions).save(any(GameSession.class));
    }

    @Test
    void chooseOption_movesToNextSectionAndFinishesOnEnd() {
        var session = existing("u1");
        when(books.getContent("b1")).thenReturn(content());

        var state = service.chooseOption("u1", session.getId(), 0, session.getVersion());

        assertEquals("s2", state.section().id());
        assertEquals(GameStatus.FINISHED, state.gameSession().getStatus());
    }

    @Test
    void chooseOption_losingAllHealthKillsPlayer() {
        var session = existing("u1");
        when(books.getContent("b1")).thenReturn(content());

        var state = service.chooseOption("u1", session.getId(), 1, session.getVersion());

        assertEquals(0, state.gameSession().getHealth());
        assertEquals(GameStatus.DEAD, state.gameSession().getStatus());
    }

    @Test
    void chooseOption_ofAnotherUser_isNotFound() {
        var session = existing("owner");

        assertThrows(NotFoundException.class,
                () -> service.chooseOption("intruder", session.getId(), 0, session.getVersion()));
        verifyNoInteractions(books);
    }

    @Test
    void chooseOption_onFinishedGame_isRejected() {
        var session = existing("u1");
        session.moveTo("s2", 10, GameStatus.FINISHED);

        assertThrows(GameFinishedException.class,
                () -> service.chooseOption("u1", session.getId(), 0, session.getVersion()));
    }

    @Test
    void chooseOption_withStaleVersion_isRejected() {
        var session = existing("u1");
        session.setVersion(2);

        assertThrows(ConflictException.class,
                () -> service.chooseOption("u1", session.getId(), 0, 1));

        verifyNoInteractions(books);
    }

    @Test
    void restartGame_abandonsPreviousAndCreatesNewSession() {
        var previous = existing("u1");
        when(books.getContent("b1")).thenReturn(content());
        when(sessions.save(any())).thenAnswer(i -> i.getArgument(0));

        var state = service.restartGame("u1", previous.getId());

        assertEquals(GameStatus.ABANDONED, previous.getStatus());
        assertNotEquals(previous.getId(), state.gameSession().getId());
        assertTrue(state.gameSession().isInProgress());
        assertEquals("s1", state.section().id());
    }

    @Test
    void resume_withoutGameInProgress_returnsEmpty() {
        when(sessions.findFirstByUserIdAndBookIdAndStatusOrderByUpdatedAtDesc(
                "u1", "b1", GameStatus.IN_PROGRESS)).thenReturn(Optional.empty());

        assertTrue(service.resumeGameInProgress("u1", "b1").isEmpty());
    }
}