package pictec.adventurebook.domain.logic;

import org.junit.jupiter.api.Test;

import pictec.adventurebook.domain.entities.BookContent;
import pictec.adventurebook.domain.exceptions.InvalidBookException;

import static pictec.adventurebook.domain.logic.Books.*;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BookValidatorTest {

    private final BookValidator validator = new BookValidator();

    @Test
    void acceptsValidBook() {
        assertThatCode(() -> validator.validate(valid())).doesNotThrowAnyException();
    }

    @Test
    void rejectsBookWithoutBegin() {
        BookContent book = book(node("1"), end("2"));
        assertThatThrownBy(() -> validator.validate(book))
                .isInstanceOf(InvalidBookException.class);
    }

    @Test
    void rejectsBookWithMoreThanOneBegin() {
        BookContent book = book(begin("1", go("3")), begin("2", go("3")), end("3"));
        assertThatThrownBy(() -> validator.validate(book))
                .isInstanceOf(InvalidBookException.class);
    }

    @Test
    void rejectsBookWithoutEnd() {
        BookContent book = book(begin("1", go("2")), node("2", go("1")));
        assertThatThrownBy(() -> validator.validate(book))
                .isInstanceOf(InvalidBookException.class);
    }

    @Test
    void rejectsOptionPointingToUnknownSection() {
        BookContent book = book(begin("1", go("99")), end("2"));
        assertThatThrownBy(() -> validator.validate(book))
                .isInstanceOf(InvalidBookException.class);
    }

    @Test
    void rejectsNonEndSectionWithoutOptions() {
        BookContent book = book(begin("1", go("2")), node("2"), end("3"));
        assertThatThrownBy(() -> validator.validate(book))
                .isInstanceOf(InvalidBookException.class);
    }
}
