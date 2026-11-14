package pictec.adventurebook.domain.entities;

import pictec.adventurebook.domain.enums.ConsequenceType;

public record Consequence(ConsequenceType type, int value, String text) {
}
