package pictec.adventurebook.domain.entities;

import java.util.List;

import pictec.adventurebook.domain.enums.SectionType;

public record BookSection(String id, String text, SectionType type, List<BookSectionOption> options) {
}
