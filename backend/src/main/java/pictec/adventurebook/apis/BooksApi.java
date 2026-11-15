package pictec.adventurebook.apis;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import pictec.adventurebook.domain.entities.BookMetadata;
import pictec.adventurebook.domain.enums.BookDifficulty;
import pictec.adventurebook.domain.exceptions.InvalidBookException;
import pictec.adventurebook.services.BookApplicationService;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BooksApi {

    private final BookApplicationService service;

    @GetMapping
    public List<BookMetadata> list(@RequestParam(required = false) String q,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) BookDifficulty difficulty) {
        return service.list(q, category, difficulty);
    }

    @GetMapping("/{id}")
    public BookMetadata get(@PathVariable String id) {
        return service.get(id);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BookMetadata> insert(@RequestPart("file") MultipartFile file,
            @Valid @RequestPart("metadata") BookMetadata metadata) {
        var created = service.insert(readBytes(file), metadata);
        return ResponseEntity.created(URI.create("/api/books/" + created.getId())).body(created);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        service.delete(id);
    }

    private byte[] readBytes(MultipartFile file) {
        if (file.isEmpty()) {
            throw new InvalidBookException("Book file is empty");
        }
        try {
            return file.getBytes();
        } catch (IOException e) {
            throw new InvalidBookException("Unreadable book file");
        }
    }
}