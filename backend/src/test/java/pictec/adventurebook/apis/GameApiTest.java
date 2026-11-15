package pictec.adventurebook.apis;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import pictec.adventurebook.domain.entities.*;
import pictec.adventurebook.domain.enums.SectionType;
import pictec.adventurebook.domain.exceptions.ConflictException;
import pictec.adventurebook.domain.exceptions.GameFinishedException;
import pictec.adventurebook.domain.exceptions.InvalidChoiceException;
import pictec.adventurebook.domain.exceptions.NotFoundException;
import pictec.adventurebook.services.GameApplicationService;
import pictec.adventurebook.services.PlayState;

@WebMvcTest(GameApi.class)
class GameApiTest {

    @Autowired
    MockMvc mvc;
    @MockitoBean
    GameApplicationService service;

    private PlayState state() {
        var section = new BookSection("s1", "start", SectionType.BEGIN,
                List.of(new BookSectionOption("go", "s2", null)));
        return new PlayState(new GameSession("u1", "b1", "s1", 10), section);
    }

    @Test
    void start_returns201WithSectionTextAndOptions() throws Exception {
        when(service.startGame("u1", "b1")).thenReturn(state());

        mvc.perform(post("/api/games").param("userId", "u1").param("bookId", "b1"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.gameSession.currentBookSectionId").value("s1"))
                .andExpect(jsonPath("$.section.text").value("start"))
                .andExpect(jsonPath("$.section.options[0].description").value("go"));
    }

    @Test
    void start_withoutUserId_returns400() throws Exception {
        mvc.perform(post("/api/games").param("bookId", "b1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void resume_withoutGame_returns204() throws Exception {
        when(service.resumeGameInProgress("u1", "b1")).thenReturn(Optional.empty());

        mvc.perform(get("/api/games/in-progress").param("userId", "u1").param("bookId", "b1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void choose_unknownGame_returns404() throws Exception {
        when(service.chooseOption("u1", "g1", 0, 0L))
                .thenThrow(new NotFoundException("Game not found"));

        mvc.perform(post("/api/games/g1/options/0").param("userId", "u1").param("version", "0"))
                .andExpect(status().isNotFound());
    }

    @Test
    void choose_finishedGame_returns409() throws Exception {
        when(service.chooseOption("u1", "g1", 0, 0L)).thenThrow(new GameFinishedException("over"));

        mvc.perform(post("/api/games/g1/options/0").param("userId", "u1").param("version", "0"))
                .andExpect(status().isConflict());
    }

    @Test
    void choose_invalidOption_returns400() throws Exception {
        when(service.chooseOption("u1", "g1", 9, 0L)).thenThrow(new InvalidChoiceException("bad index"));

        mvc.perform(post("/api/games/g1/options/9").param("userId", "u1").param("version", "0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void choose_nonNumericOption_returns400() throws Exception {
        mvc.perform(post("/api/games/g1/options/abc").param("userId", "u1").param("version", "0"))
                .andExpect(status().isBadRequest());
    }
}