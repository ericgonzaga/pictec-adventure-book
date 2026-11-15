package pictec.adventurebook.support;

import java.util.List;

import pictec.adventurebook.domain.entities.BookContent;
import pictec.adventurebook.domain.entities.BookMetadata;
import pictec.adventurebook.domain.entities.BookSection;
import pictec.adventurebook.domain.entities.BookSectionOption;
import pictec.adventurebook.domain.entities.Consequence;
import pictec.adventurebook.domain.enums.BookDifficulty;
import pictec.adventurebook.domain.enums.ConsequenceType;
import pictec.adventurebook.domain.enums.SectionType;

public final class BookFixtures {

        private BookFixtures() {
        }

        // ---------- JSON (service / flow) ----------

        public static final String VALID_JSON = """
                        {"title":"T","author":"A","sections":[
                          {"id":"s1","text":"start","type":"BEGIN","options":[
                            {"description":"go","gotoId":"s2","consequence":null},
                            {"description":"trap","gotoId":"s2",
                             "consequence":{"type":"LOSE_HEALTH","value":10,"text":"ouch"}}]},
                          {"id":"s2","text":"end","type":"END","options":[]}]}
                        """;

        public static final String NO_END_JSON = """
                        {"title":"T","author":"A","sections":[
                          {"id":"s1","text":"start","type":"BEGIN","options":[
                            {"description":"loop","gotoId":"s1","consequence":null}]}]}
                        """;

        // ---------- domain ----------

        public static BookSectionOption go(String toId) {
                return new BookSectionOption("go to " + toId, toId, null);
        }

        public static BookSectionOption heal(String toId, int points) {
                return new BookSectionOption("go to " + toId, toId,
                                new Consequence(ConsequenceType.GAIN_HEALTH, points, "healed"));
        }

        public static BookSectionOption damage(String toId, int points) {
                return new BookSectionOption("go to " + toId, toId,
                                new Consequence(ConsequenceType.LOSE_HEALTH, points, "ouch"));
        }

        public static BookSection begin(String id, BookSectionOption... options) {
                return new BookSection(id, "text " + id, SectionType.BEGIN, List.of(options));
        }

        public static BookSection node(String id, BookSectionOption... options) {
                return new BookSection(id, "text " + id, SectionType.NODE, List.of(options));
        }

        public static BookSection end(String id) {
                return new BookSection(id, "text " + id, SectionType.END, List.of());
        }

        public static BookContent book(BookSection... sections) {
                return new BookContent("Title", "Author", List.of(sections));
        }

        /** 1 (BEGIN) -> 2 (NODE) -> 3 (END). */
        public static BookContent valid() {
                return book(begin("1", go("2")), node("2", go("3")), end("3"));
        }

        // ---------- service ----------

        public static BookContent content() {
                return new BookContent("T", "A", List.of(
                                new BookSection("s1", "start", SectionType.BEGIN, List.of(
                                                new BookSectionOption("go", "s2", null),
                                                new BookSectionOption("trap", "s2",
                                                                new Consequence(ConsequenceType.LOSE_HEALTH, 10,
                                                                                "ouch")))),
                                new BookSection("s2", "end", SectionType.END, List.of())));
        }

        public static BookMetadata metadata(BookDifficulty difficulty) {
                var m = new BookMetadata();
                m.setDescription("d");
                m.setCategory("c");
                m.setDifficulty(difficulty);
                return m;
        }
}