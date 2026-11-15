package pictec.adventurebook.services;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import pictec.adventurebook.domain.entities.GameSession;
import pictec.adventurebook.domain.enums.GameStatus;
import pictec.adventurebook.domain.exceptions.GameFinishedException;
import pictec.adventurebook.domain.exceptions.NotFoundException;
import pictec.adventurebook.domain.logic.GameEngine;
import pictec.adventurebook.services.irespositories.IGameSessionRepository;

@Service
@RequiredArgsConstructor
public class GameApplicationService {

    private final BookApplicationService books;
    private final IGameSessionRepository sessions;
    private final GameEngine engine;

    @Transactional(readOnly = true)
    public PlayState getGame(String userId, String gameSessionId) {
        var session = loadOwned(userId, gameSessionId);
        var section = books.getSection(session.getBookId(), session.getCurrentBookSectionId());
        return new PlayState(session, section);
    }

    @Transactional
    public PlayState startGame(String userId, String bookId) {
        var game = engine.start(userId, bookId, books.getContent(bookId));
        var session = sessions.save(game);
        var section = books.getSection(bookId, session.getCurrentBookSectionId());
        return new PlayState(session, section);
    }

    @Transactional(readOnly = true)
    public Optional<PlayState> resumeGameInProgress(String userId, String bookId) {
        return sessions
                .findFirstByUserIdAndBookIdAndStatusOrderByUpdatedAtDesc(userId, bookId, GameStatus.IN_PROGRESS)
                .map(s -> new PlayState(s, books.getSection(bookId, s.getCurrentBookSectionId())));
    }

    @Transactional
    public PlayState chooseOption(String userId, String gameSessionId, int optionIndex) {
        var session = loadOwned(userId, gameSessionId);
        if (session.getStatus() != GameStatus.IN_PROGRESS) {
            throw new GameFinishedException("Game is not in progress: " + gameSessionId);
        }

        var content = books.getContent(session.getBookId());
        var nextSection = engine.choose(session, content, optionIndex);
        return new PlayState(session, nextSection);
    }

    @Transactional
    public PlayState restartGame(String userId, String gameSessionId) {
        var previousGame = loadOwned(userId, gameSessionId);
        if (previousGame.isInProgress()) {
            previousGame.abandon();
        }

        var content = books.getContent(previousGame.getBookId());
        var newGame = engine.start(userId, previousGame.getBookId(), content);
        var session = sessions.save(newGame);
        var section = content.findSection(session.getCurrentBookSectionId()).orElseThrow();
        return new PlayState(session, section);
    }

    private GameSession loadOwned(String userId, String gameSessionId) {
        return sessions.findById(gameSessionId)
                .filter(s -> s.getUserId().equals(userId))
                .orElseThrow(() -> new NotFoundException("Game not found: " + gameSessionId));
    }
}
