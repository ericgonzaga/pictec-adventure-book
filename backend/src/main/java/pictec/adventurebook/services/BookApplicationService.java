package pictec.adventurebook.services;

import java.util.List;
import java.util.UUID;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import pictec.adventurebook.apis.dtos.BookMetadataRequest;
import pictec.adventurebook.domain.entities.BookContent;
import pictec.adventurebook.domain.entities.BookMetadata;
import pictec.adventurebook.domain.entities.BookSection;
import pictec.adventurebook.domain.exceptions.ConflictException;
import pictec.adventurebook.domain.exceptions.InvalidBookException;
import pictec.adventurebook.domain.exceptions.NotFoundException;
import pictec.adventurebook.domain.logic.BookValidator;
import pictec.adventurebook.services.respositories.BookMetadataRepository;
import pictec.adventurebook.services.respositories.GameSessionRepository;
import pictec.adventurebook.services.storage.BookContentStorage;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class BookApplicationService {

    private static final Logger log = LoggerFactory.getLogger(BookApplicationService.class);

    private final BookMetadataRepository metadataRepository;
    private final GameSessionRepository sessions;
    private final BookContentStorage storage;
    private final BookValidator validator;
    private final ObjectMapper mapper;

    public BookSection getSection(String bookId, String sectionId) {
        return getContent(bookId).findSection(sectionId)
                .orElseThrow(() -> new NotFoundException("Section not found: " + sectionId));
    }

    public List<BookMetadata> list(String query, String category, String difficulty) {
        return metadataRepository.search(query == null ? "" : query.trim(),
                blankToNull(category), blankToNull(difficulty));
    }

    public BookMetadata get(String id) {
        return metadataRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Book not found: " + id));
    }

    @Cacheable(value = "bookContent", key = "#bookId")
    public BookContent getContent(String bookId) {
        return parse(storage.load(get(bookId).getContentKey()));
    }

    public BookMetadata insert(byte[] file, BookMetadataRequest metadata) {
        BookContent content = parse(file);
        if (isBlank(content.title()) || isBlank(content.author())) {
            throw new InvalidBookException("Book must have title and author");
        }
        validator.validate(content);

        String id = UUID.randomUUID().toString();
        String key = "books/" + id + "/" + UUID.randomUUID() + ".json";

        try {
            storage.save(key, file);

            var newMetadata = new BookMetadata(
                    id,
                    content.title(),
                    content.author(),
                    metadata.description(),
                    metadata.category(),
                    metadata.difficulty(),
                    key);
            return metadataRepository.save(newMetadata);
        } catch (RuntimeException e) {
            deleteFileQuietly(key);
            throw e;
        }
    }

    @CacheEvict(value = "bookContent", key = "#id")
    public void delete(String id) {
        BookMetadata book = get(id);
        if (sessions.existsByBookId(id)) {
            throw new ConflictException("Book has games associated and cannot be deleted");
        }
        metadataRepository.delete(book);
        deleteFileQuietly(book.getContentKey());
    }

    private BookContent parse(byte[] bytes) {
        try {
            return mapper.readValue(bytes, BookContent.class);
        } catch (JacksonException e) {
            throw new InvalidBookException("Invalid book JSON: " + e.getOriginalMessage());
        }
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