package pictec.adventurebook.apis.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import pictec.adventurebook.domain.enums.BookDifficulty;

public record BookMetadataRequest(@Size(max = 1000) String description,
        @Size(max = 100) String category,
        @NotNull BookDifficulty difficulty) {
}
