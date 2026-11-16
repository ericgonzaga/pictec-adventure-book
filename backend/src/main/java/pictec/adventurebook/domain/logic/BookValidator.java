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

    private final int maxConsequenceHealth;

    public BookValidator(int maxConsequenceHealth) {
        if (maxConsequenceHealth <= 0) {
            throw new IllegalArgumentException("Max consequence health must be greater than zero");
        }
        this.maxConsequenceHealth = maxConsequenceHealth;
    }

    public void validate(BookContent book) {
        if (book == null) {
            throw new InvalidBookException("Book must not be null");
        }

        List<String> errors = new ArrayList<>();

        if (isBlank(book.title()) || isBlank(book.author())) {
            errors.add("Book must have title and author");
        }

        List<BookSection> sections = book.sections() == null ? List.of() : book.sections();

        errors.addAll(validateSections(sections));

        long begins = sections.stream()
                .filter(s -> s != null && s.type() == SectionType.BEGIN)
                .count();

        if (begins != 1) {
            errors.add("Book must have exactly one BEGIN section, found " + begins);
        }

        if (sections.stream().noneMatch(s -> s != null && s.type() == SectionType.END)) {
            errors.add("Book must have at least one END section");
        }

        if (!errors.isEmpty()) {
            throw new InvalidBookException(String.join("; ", errors));
        }
    }

    private List<String> validateSections(List<BookSection> sections) {
        List<String> errors = new ArrayList<>();
        Set<String> ids = new HashSet<>();

        for (BookSection section : sections) {
            if (section == null) {
                errors.add("Book contains a null section");
                continue;
            }

            if (isBlank(section.id())) {
                errors.add("Section must have an id");
            } else if (!ids.add(section.id())) {
                errors.add("Duplicate section id: " + section.id());
            }

            if (section.type() == null) {
                errors.add("Section " + section.id() + " must have a type");
            }
        }

        for (BookSection section : sections) {
            if (section == null) {
                continue;
            }

            List<BookSectionOption> options = section.options() == null ? List.of() : section.options();

            if (section.type() != SectionType.END && options.isEmpty()) {
                errors.add("Non-ending section " + section.id() + " has no options");
            }

            for (BookSectionOption option : options) {
                if (option == null) {
                    errors.add("Section " + section.id() + " contains a null option");
                    continue;
                }

                if (isBlank(option.gotoId()) || !ids.contains(option.gotoId())) {
                    errors.add("Section " + section.id()
                            + " points to unknown section " + option.gotoId());
                }

                var consequence = option.consequence();
                if (consequence != null) {
                    if (consequence.type() == null) {
                        errors.add("Consequence in section " + section.id() + " must have a type");
                    }
                    if (consequence.value() < 0 || consequence.value() > this.maxConsequenceHealth) {
                        errors.add("Consequence in section " + section.id()
                                + " must have a value between 0 and " + this.maxConsequenceHealth);
                    }
                }
            }
        }

        return errors;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}