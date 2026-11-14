package pictec.adventurebook.services;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import pictec.adventurebook.domain.enums.GameStatus;
import pictec.adventurebook.domain.exceptions.NotFoundException;
import pictec.adventurebook.domain.logic.GameEngine;
import pictec.adventurebook.services.respositories.GameSessionRepository;

@Service
@RequiredArgsConstructor
public class GameApplicationService {

    private final BookApplicationService books;
    private final GameSessionRepository sessions;
    private final GameEngine engine;

    @Transactional
    public PlayState startGame(String userId, String bookId) {
        var game = engine.start(userId, bookId, books.getContent(bookId))
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
        var session = sessions.findById(gameSessionId)
                .filter(s -> s.getUserId().equals(userId))
                .orElseThrow(() -> new NotFoundException("Game not found: " + gameSessionId));

        var content = books.getContent(session.getBookId());
        var nextSection = engine.choose(session, content, optionIndex);
        return new PlayState(session, nextSection);
    }
}
