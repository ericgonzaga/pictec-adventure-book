package pictec.adventurebook.domain.entities;

import java.util.List;
import java.util.Optional;

import pictec.adventurebook.domain.enums.SectionType;

public record BookContent(String title, String author, List<BookSection> sections) {

    public Optional<BookSection> findSection(String id) {
        return sections.stream().filter(s -> s.id().equals(id)).findFirst();
    }

    public Optional<BookSection> findBeginSection() {
        return sections.stream().filter(s -> s.type() == SectionType.BEGIN).findFirst();
    }
}
