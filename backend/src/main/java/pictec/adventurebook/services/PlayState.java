package pictec.adventurebook.services;

import pictec.adventurebook.domain.entities.BookSection;
import pictec.adventurebook.domain.entities.GameSession;

public record PlayState(GameSession gameSession, BookSection section) {
}
