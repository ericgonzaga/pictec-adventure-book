package pictec.adventurebook.domain.logic;

import pictec.adventurebook.domain.entities.BookContent;
import pictec.adventurebook.domain.entities.Consequence;
import pictec.adventurebook.domain.entities.GameSession;
import pictec.adventurebook.domain.entities.InvalidChoiceException;
import pictec.adventurebook.domain.entities.BookSection;
import pictec.adventurebook.domain.entities.BookSectionOption;
import pictec.adventurebook.domain.enums.ConsequenceType;
import pictec.adventurebook.domain.enums.GameStatus;
import pictec.adventurebook.domain.enums.SectionType;
import pictec.adventurebook.domain.exceptions.GameFinishedException;
import pictec.adventurebook.domain.exceptions.InvalidBookException;

public class GameEngine {

    private final int initialHealth;

    public GameEngine(int initialHealth) {
        this.initialHealth = initialHealth;
    }

    public GameSession start(String userId, String bookId, BookContent content) {
        BookSection begin = content.findBeginSection()
                .orElseThrow(() -> new InvalidBookException("Book has no BEGIN section"));

        return new GameSession(userId, bookId, begin.id(), initialHealth);
    }

    public BookSection choose(GameSession session, BookContent content, int optionIndex) {
        if (!session.isInProgress()) {
            throw new GameFinishedException("Game is already over: " + session.getStatus());
        }

        BookSection current = findBookSection(content, session.getCurrentBookSectionId());
        if (optionIndex < 0 || optionIndex >= current.options().size()) {
            throw new InvalidChoiceException("Invalid option index: " + optionIndex);
        }

        BookSectionOption option = current.options().get(optionIndex);
        int health = applyConsequence(session.getHealth(), option.consequence());
        BookSection next = findBookSection(content, option.gotoId());

        session.moveTo(next.id(), health, statusOf(health, next));
        return next;
    }

    private BookSection findBookSection(BookContent content, String id) {
        return content.findSection(id)
                .orElseThrow(() -> new InvalidBookException("Unknown section: " + id));
    }

    private int applyConsequence(int health, Consequence consequence) {
        if (consequence == null)
            return health;
        return switch (consequence.type()) {
            case ConsequenceType.LOSE_HEALTH -> health - consequence.value();
            case ConsequenceType.GAIN_HEALTH -> health + consequence.value();
            default -> health;
        };
    }

    private GameStatus statusOf(int health, BookSection next) {
        if (health <= 0) {
            return GameStatus.DEAD;
        }
        return next.type() == SectionType.END ? GameStatus.FINISHED : GameStatus.IN_PROGRESS;
    }
}