package pictec.adventurebook.domain.logic;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static pictec.adventurebook.support.BookFixtures.*;

import org.junit.jupiter.api.Test;

import pictec.adventurebook.domain.entities.BookContent;
import pictec.adventurebook.domain.exceptions.InvalidBookException;

class BookValidatorTest {

    private final BookValidator validator = new BookValidator(100);

    @Test
    void acceptsValidBook() {
        assertThatCode(() -> validator.validate(valid())).doesNotThrowAnyException();
    }

    @Test
    void rejectsBookWithoutBegin() {
        assertInvalid(book(node("1", go("2")), end("2")));
    }

    @Test
    void rejectsBookWithMoreThanOneBegin() {
        assertInvalid(book(begin("1", go("3")), begin("2", go("3")), end("3")));
    }

    @Test
    void rejectsBookWithoutEnd() {
        assertInvalid(book(begin("1", go("2")), node("2", go("1"))));
    }

    @Test
    void rejectsOptionPointingToUnknownSection() {
        assertInvalid(book(begin("1", go("99")), end("2")));
    }

    @Test
    void rejectsNonEndSectionWithoutOptions() {
        assertInvalid(book(begin("1", go("2")), node("2"), end("3")));
    }

    private void assertInvalid(BookContent book) {
        assertThatThrownBy(() -> validator.validate(book))
                .isInstanceOf(InvalidBookException.class);
    }

    @Test
    void rejectsConsequenceValueAboveMax() {
        assertInvalid(book(begin("1", damage("2", 101)), end("2")));
    }

    @Test
    void acceptsConsequenceValueAtMax() {
        assertThatCode(() -> validator.validate(book(begin("1", heal("2", 100)), end("2"))))
                .doesNotThrowAnyException();
    }

    @Test
    void rejectsNegativeConsequenceValue() {
        assertInvalid(book(begin("1", damage("2", -1)), end("2")));
    }
}