package pictec.adventurebook.domain.logic;

import org.junit.jupiter.api.Test;

import pictec.adventurebook.domain.entities.BookContent;
import pictec.adventurebook.domain.entities.InvalidChoiceException;
import pictec.adventurebook.domain.enums.GameStatus;
import pictec.adventurebook.domain.exceptions.GameFinishedException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static pictec.adventurebook.domain.logic.Books.*;

class GameEngineTest {

    private final GameEngine engine = new GameEngine(10);
    private static final String USER_ID = "user";
    private static final String BOOK_ID = "book";

    @Test
    void startsAtBeginSectionWithTenHealth() {
        var game = engine.start(USER_ID, BOOK_ID, valid());

        assertThat(game.getCurrentBookSectionId()).isEqualTo("1");
        assertThat(game.getHealth()).isEqualTo(10);
        assertThat(game.getStatus()).isEqualTo(GameStatus.IN_PROGRESS);
    }

    @Test
    void choiceWithoutConsequenceMovesAndKeepsHealth() {
        var book = valid();
        var session = engine.start(USER_ID, BOOK_ID, book);
        var section = engine.choose(session, book, 0);

        assertThat(session.getCurrentBookSectionId()).isEqualTo("2");
        assertThat(session.getHealth()).isEqualTo(10);
        assertThat(session.getStatus()).isEqualTo(GameStatus.IN_PROGRESS);
        assertThat(section.id()).isEqualTo("2");
    }

    @Test
    void choiceWithConsequenceReducesHealth() {
        BookContent book = book(begin("1", damage("2", 3)), end("2"));
        var session = engine.start(USER_ID, BOOK_ID, book);
        var section = engine.choose(session, book, 0);

        assertThat(session.getHealth()).isEqualTo(7);
        assertThat(session.getStatus()).isEqualTo(GameStatus.FINISHED);
        assertThat(section.id()).isEqualTo("2");
    }

    @Test
    void choiceWithConsequenceIncreaseHealth() {
        BookContent book = book(begin("1", heal("2", 3)), end("2"));
        var session = engine.start(USER_ID, BOOK_ID, book);
        var section = engine.choose(session, book, 0);

        assertThat(session.getHealth()).isEqualTo(13);
        assertThat(session.getStatus()).isEqualTo(GameStatus.FINISHED);
        assertThat(section.id()).isEqualTo("2");
    }

    @Test
    void gameEndsWithDeathWhenHealthReachesZeroEvenOnNonEndSection() {
        BookContent book = book(begin("1", damage("2", 10)), node("2", go("3")), end("3"));
        var session = engine.start(USER_ID, BOOK_ID, book);
        engine.choose(session, book, 0);

        assertThat(session.getHealth()).isLessThanOrEqualTo(0);
        assertThat(session.getStatus()).isEqualTo(GameStatus.DEAD);
    }

    @Test
    void reachingEndSectionFinishesGameAndBlocksFurtherChoices() {
        var book = valid();
        var session = engine.start(USER_ID, BOOK_ID, book);
        engine.choose(session, book, 0);
        engine.choose(session, book, 0);

        assertThat(session.getCurrentBookSectionId()).isEqualTo("3");
        assertThat(session.getStatus()).isEqualTo(GameStatus.FINISHED);
        assertThatThrownBy(() -> engine.choose(session, book, 0))
                .isInstanceOf(GameFinishedException.class);
    }

    @Test
    void rejectsOptionIndexOutsideCurrentSection() {
        var book = valid();
        var session = engine.start(USER_ID, BOOK_ID, book);

        assertThatThrownBy(() -> engine.choose(session, book, 5))
                .isInstanceOf(InvalidChoiceException.class);
        assertThatThrownBy(() -> engine.choose(session, book, -1))
                .isInstanceOf(InvalidChoiceException.class);
    }
}
