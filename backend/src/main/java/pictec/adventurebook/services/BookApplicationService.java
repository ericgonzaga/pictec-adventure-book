package pictec.adventurebook.services;

import java.util.List;
import java.util.UUID;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import pictec.adventurebook.domain.entities.BookContent;
import pictec.adventurebook.domain.entities.BookMetadata;
import pictec.adventurebook.domain.entities.BookSection;
import pictec.adventurebook.domain.enums.BookDifficulty;
import pictec.adventurebook.domain.exceptions.ConflictException;
import pictec.adventurebook.domain.exceptions.InvalidBookException;
import pictec.adventurebook.domain.exceptions.NotFoundException;
import pictec.adventurebook.domain.logic.BookValidator;
import pictec.adventurebook.services.irespositories.IBookMetadataRepository;
import pictec.adventurebook.services.irespositories.IGameSessionRepository;
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

    public BookSection getSection(String bookId, String sectionId) {
        return getContent(bookId).findSection(sectionId)
                .orElseThrow(() -> new NotFoundException("Section not found: " + sectionId));
    }

    public List<BookMetadata> list(String query, String category, BookDifficulty difficulty) {
        return metadataRepository.search(query == null ? "" : query.trim(),
                blankToNull(category), difficulty);
    }

    public BookMetadata get(String id) {
        return metadataRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Book not found: " + id));
    }

    public BookContent getContent(String bookId) {
        return parse(storage.load(get(bookId).getContentKey()));
    }

    public BookMetadata insert(byte[] file, BookMetadata metadata) {
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
                    metadata.getDescription(),
                    metadata.getCategory(),
                    metadata.getDifficulty(),
                    key);
            return metadataRepository.save(newMetadata);
        } catch (RuntimeException e) {
            deleteFileQuietly(key);
            throw e;
        }
    }

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