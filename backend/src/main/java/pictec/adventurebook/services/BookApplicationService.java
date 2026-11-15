package pictec.adventurebook.services;

import java.util.List;
import java.util.UUID;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import lombok.RequiredArgsConstructor;
import pictec.adventurebook.domain.entities.BookContent;
import pictec.adventurebook.domain.entities.BookMetadata;
import pictec.adventurebook.domain.entities.BookSection;
import pictec.adventurebook.domain.enums.BookDifficulty;
import pictec.adventurebook.domain.exceptions.ConflictException;
import pictec.adventurebook.domain.exceptions.InvalidBookException;
import pictec.adventurebook.domain.exceptions.NotFoundException;
import pictec.adventurebook.domain.logic.BookValidator;
import pictec.adventurebook.services.irepositories.IBookMetadataRepository;
import pictec.adventurebook.services.irepositories.IGameSessionRepository;
import pictec.adventurebook.services.istorage.IBookContentStorage;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class BookApplicationService {

    private static final Logger log = LoggerFactory.getLogger(BookApplicationService.class);

    private final IBookMetadataRepository metadataRepository;
    private final IGameSessionRepository sessions;
    private final IBookContentStorage storage;
    private final BookValidator validator;
    private final ObjectMapper mapper;

    @Transactional(readOnly = true)
    public BookSection getSection(String bookId, String sectionId) {
        return getContent(bookId).findSection(sectionId)
                .orElseThrow(() -> new NotFoundException("Section not found: " + sectionId));
    }

    @Transactional(readOnly = true)
    public List<BookMetadata> list(String query, String category, BookDifficulty difficulty) {
        return metadataRepository.search(query == null ? "" : query.trim(),
                blankToNull(category), difficulty);
    }

    @Transactional(readOnly = true)
    public BookMetadata get(String id) {
        return metadataRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Book not found: " + id));
    }

    @Transactional(readOnly = true)
    public BookContent getContent(String bookId) {
        return parse(storage.load(get(bookId).getContentKey()));
    }

    @Transactional
    public BookMetadata insert(byte[] file, BookMetadata metadata) {
        BookContent content = parse(file);
        validator.validate(content);

        String id = UUID.randomUUID().toString();
        String key = "books/" + id + "/" + UUID.randomUUID() + ".json";

        storage.save(key, file);
        cleanupFileIfNotCommitted(key);

        var newMetadata = new BookMetadata(
                id,
                content.title(),
                content.author(),
                metadata.getDescription(),
                metadata.getCategory(),
                metadata.getDifficulty(),
                key);
        return metadataRepository.saveAndFlush(newMetadata);
    }

    @Transactional
    public void delete(String id) {
        BookMetadata book = get(id);
        if (sessions.existsByBookId(id)) {
            throw new ConflictException("Book has games associated and cannot be deleted");
        }
        metadataRepository.delete(book);
        metadataRepository.flush();
        deleteFileAfterCommit(book.getContentKey());
    }

    private BookContent parse(byte[] bytes) {
        try {
            return mapper.readValue(bytes, BookContent.class);
        } catch (JacksonException e) {
            throw new InvalidBookException("Invalid book JSON: " + e.getOriginalMessage());
        }
    }

    // If the transaction is rolled back, we want to delete the file we just saved
    private void cleanupFileIfNotCommitted(String key) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status != STATUS_COMMITTED) {
                    deleteFileQuietly(key);
                }
            }
        });
    }

    // If the transaction is committed, we want to delete the file after the commit
    private void deleteFileAfterCommit(String key) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            deleteFileQuietly(key);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                deleteFileQuietly(key);
            }
        });
    }

    private void deleteFileQuietly(String key) {
        try {
            storage.delete(key);
        } catch (RuntimeException e) {
            log.warn("Orphan book file, remove manually: {}", key, e);
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private static String blankToNull(String s) {
        return isBlank(s) ? null : s.trim();
    }
}