package pictec.adventurebook.domain.logic;

import java.util.List;

import pictec.adventurebook.domain.entities.BookContent;
import pictec.adventurebook.domain.entities.Consequence;
import pictec.adventurebook.domain.entities.BookSection;
import pictec.adventurebook.domain.entities.BookSectionOption;
import pictec.adventurebook.domain.enums.ConsequenceType;
import pictec.adventurebook.domain.enums.SectionType;

final class Books {
    private Books() {
    }

    static BookSectionOption go(String toId) {
        return new BookSectionOption("go to " + toId, toId, null);
    }

    static BookSectionOption heal(String toId, int points) {
        return new BookSectionOption("go to " + toId, toId,
                new Consequence(ConsequenceType.GAIN_HEALTH, points, "healed"));
    }

    static BookSectionOption damage(String toId, int points) {
        return new BookSectionOption("go to " + toId, toId,
                new Consequence(ConsequenceType.LOSE_HEALTH, points, "ouch"));
    }

    static BookSection begin(String id, BookSectionOption... options) {
        return new BookSection(id, "text " + id, SectionType.BEGIN, List.of(options));
    }

    static BookSection node(String id, BookSectionOption... options) {
        return new BookSection(id, "text " + id, SectionType.NODE, List.of(options));
    }

    static BookSection end(String id) {
        return new BookSection(id, "text " + id, SectionType.END, List.of());
    }

    static BookContent book(BookSection... sections) {
        return new BookContent("Title", "Author", List.of(sections));
    }

    /** 1(BEGIN) -> 2(NODE) -> 3(END). */
    static BookContent valid() {
        return book(begin("1", go("2")), node("2", go("3")), end("3"));
    }
}