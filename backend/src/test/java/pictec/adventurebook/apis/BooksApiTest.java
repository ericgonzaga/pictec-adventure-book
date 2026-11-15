package pictec.adventurebook.apis;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import pictec.adventurebook.domain.entities.BookMetadata;
import pictec.adventurebook.domain.enums.BookDifficulty;
import pictec.adventurebook.domain.exceptions.ConflictException;
import pictec.adventurebook.domain.exceptions.InvalidBookException;
import pictec.adventurebook.domain.exceptions.NotFoundException;
import pictec.adventurebook.services.BookApplicationService;

@WebMvcTest(BooksApi.class)
class BooksApiTest {

    @Autowired
    MockMvc mvc;
    @MockitoBean
    BookApplicationService service;

    private final BookMetadata book = new BookMetadata("b1", "T", "A", "d", "c", BookDifficulty.EASY,
            "books/b1/x.json");

    private final MockMultipartFile file = new MockMultipartFile("file", "book.json", "application/json",
            "{}".getBytes());
    private final MockMultipartFile metadata = new MockMultipartFile("metadata", "", "application/json",
            "{\"description\":\"d\",\"category\":\"c\",\"difficulty\":\"EASY\"}".getBytes());

    @Test
    void insert_returns201WithLocation() throws Exception {
        when(service.insert(any(), any())).thenReturn(book);

        mvc.perform(multipart("/api/books").file(file).file(metadata))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/books/b1"))
                .andExpect(jsonPath("$.id").value("b1"));
    }

    @Test
    void insert_invalidBook_returns422() throws Exception {
        when(service.insert(any(), any())).thenThrow(new InvalidBookException("no END"));

        mvc.perform(multipart("/api/books").file(file).file(metadata))
                .andExpect(status().isBadRequest());
    }

    @Test
    void insert_withoutFile_returns400() throws Exception {
        mvc.perform(multipart("/api/books").file(metadata))
                .andExpect(status().isBadRequest());
    }

    @Test
    void list_passesFiltersToService() throws Exception {
        when(service.list("x", null, BookDifficulty.EASY)).thenReturn(List.of(book));

        mvc.perform(get("/api/books").param("query", "x").param("difficulty", "EASY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("b1"));
    }

    @Test
    void list_invalidDifficulty_returns400() throws Exception {
        mvc.perform(get("/api/books").param("difficulty", "IMPOSSIBLE"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void get_unknownBook_returns404() throws Exception {
        when(service.get("x")).thenThrow(new NotFoundException("Book not found"));

        mvc.perform(get("/api/books/x")).andExpect(status().isNotFound());
    }

    @Test
    void delete_bookWithGames_returns409() throws Exception {
        doThrow(new ConflictException("has games")).when(service).delete("b1");

        mvc.perform(delete("/api/books/b1")).andExpect(status().isConflict());
    }

    @Test
    void delete_returns204() throws Exception {
        mvc.perform(delete("/api/books/b1")).andExpect(status().isNoContent());
    }
}