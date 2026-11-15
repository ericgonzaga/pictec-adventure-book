package pictec.adventurebook.services;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static pictec.adventurebook.support.BookFixtures.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pictec.adventurebook.domain.entities.BookMetadata;
import pictec.adventurebook.domain.enums.BookDifficulty;
import pictec.adventurebook.domain.exceptions.ConflictException;
import pictec.adventurebook.domain.exceptions.InvalidBookException;
import pictec.adventurebook.domain.logic.BookValidator;
import pictec.adventurebook.services.irepositories.IBookMetadataRepository;
import pictec.adventurebook.services.irepositories.IGameSessionRepository;
import pictec.adventurebook.services.istorage.IBookContentStorage;
import tools.jackson.databind.json.JsonMapper;

@ExtendWith(MockitoExtension.class)
class BookApplicationServiceTest {

    @Mock
    IBookMetadataRepository repo;
    @Mock
    IGameSessionRepository sessions;
    @Mock
    IBookContentStorage storage;

    BookApplicationService service;

    @BeforeEach
    void setUp() {
        service = new BookApplicationService(repo, sessions, storage,
                new BookValidator(), JsonMapper.builder().build());
    }

    @Test
    void insert_validBook_storesFileThenMetadata() {
        when(repo.save(any())).thenAnswer(i -> i.getArgument(0));

        var saved = service.insert(VALID_JSON.getBytes(UTF_8), metadata(BookDifficulty.EASY));

        var key = ArgumentCaptor.forClass(String.class);
        InOrder order = inOrder(storage, repo);
        order.verify(storage).save(key.capture(), any(byte[].class));
        order.verify(repo).save(any(BookMetadata.class));
        assertEquals("T", saved.getTitle());
        assertEquals(key.getValue(), saved.getContentKey());
    }

    @Test
    void insert_invalidBook_neverTouchesStorage() {
        assertThrows(InvalidBookException.class,
                () -> service.insert(NO_END_JSON.getBytes(UTF_8), metadata(BookDifficulty.EASY)));
        verifyNoInteractions(storage, repo);
    }

    @Test
    void insert_whenDbFails_removesStoredFile() {
        when(repo.save(any())).thenThrow(new RuntimeException("db down"));

        assertThrows(RuntimeException.class,
                () -> service.insert(VALID_JSON.getBytes(UTF_8), metadata(BookDifficulty.EASY)));
        verify(storage).delete(anyString());
    }

    @Test
    void delete_bookWithGames_isBlocked() {
        var book = new BookMetadata("b1", "T", "A", "d", "c", BookDifficulty.EASY, "k.json");
        when(repo.findById("b1")).thenReturn(Optional.of(book));
        when(sessions.existsByBookId("b1")).thenReturn(true);

        assertThrows(ConflictException.class, () -> service.delete("b1"));
        verify(repo, never()).delete(any());
        verify(storage, never()).delete(anyString());
    }

    @Test
    void delete_bookWithoutGames_removesMetadataAndFile() {
        var book = new BookMetadata("b1", "T", "A", "d", "c", BookDifficulty.EASY, "k.json");
        when(repo.findById("b1")).thenReturn(Optional.of(book));
        when(sessions.existsByBookId("b1")).thenReturn(false);

        service.delete("b1");

        verify(repo).delete(book);
        verify(storage).delete("k.json");
    }
}