package pictec.adventurebook.domain.logic;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import pictec.adventurebook.domain.entities.BookContent;
import pictec.adventurebook.domain.entities.BookSection;
import pictec.adventurebook.domain.entities.BookSectionOption;
import pictec.adventurebook.domain.enums.SectionType;
import pictec.adventurebook.domain.exceptions.InvalidBookException;

public class BookValidator {

    public void validate(BookContent book) {
        List<String> errors = new ArrayList<>();
        List<BookSection> sections = book.sections() == null ? List.of() : book.sections();

        errors.addAll(validateSection(sections));

        long begins = sections.stream().filter(s -> s.type() == SectionType.BEGIN).count();
        if (begins != 1) {
            errors.add("Book must have exactly one BEGIN section, found " + begins);
        }

        if (sections.stream().noneMatch(s -> s.type() == SectionType.END)) {
            errors.add("Book must have at least one END section");
        }

        if (!errors.isEmpty()) {
            throw new InvalidBookException(String.join("; ", errors));
        }
    }

    private List<String> validateSection(List<BookSection> sections) {
        List<String> errors = new ArrayList<>();

        Set<String> ids = new HashSet<>();
        for (BookSection s : sections) {
            if (!ids.add(s.id())) {
                errors.add("Duplicate section id: " + s.id());
            }
        }

        for (BookSection s : sections) {
            List<BookSectionOption> options = s.options() == null ? List.of() : s.options();

            if (s.type() != SectionType.END && options.isEmpty()) {
                errors.add("Non-ending section " + s.id() + " has no options");
            }
            for (var o : options) {
                if (!ids.contains(o.gotoId())) {
                    errors.add("Section " + s.id() + " points to unknown section " + o.gotoId());
                }
            }
        }

        return errors;
    }
}
