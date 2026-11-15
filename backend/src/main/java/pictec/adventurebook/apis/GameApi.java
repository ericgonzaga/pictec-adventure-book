package pictec.adventurebook.apis;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import pictec.adventurebook.services.GameApplicationService;
import pictec.adventurebook.services.PlayState;

@RestController
@RequestMapping("/api/games")
@RequiredArgsConstructor
public class GameApi {

    private final GameApplicationService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PlayState start(@RequestParam String userId, @RequestParam String bookId) {
        return service.startGame(userId, bookId);
    }

    @GetMapping("/in-progress")
    public ResponseEntity<PlayState> resume(@RequestParam String userId, @RequestParam String bookId) {
        return service.resumeGameInProgress(userId, bookId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping("/{gameSessionId}")
    public PlayState get(@RequestParam String userId, @PathVariable String gameSessionId) {
        return service.getGame(userId, gameSessionId);
    }

    @PostMapping("/{gameSessionId}/restart")
    @ResponseStatus(HttpStatus.CREATED)
    public PlayState restart(@RequestParam String userId, @PathVariable String gameSessionId) {
        return service.restartGame(userId, gameSessionId);
    }

    @PostMapping("/{gameSessionId}/options/{optionIndex}")
    public PlayState choose(@RequestParam String userId, @PathVariable String gameSessionId,
            @PathVariable int optionIndex) {
        return service.chooseOption(userId, gameSessionId, optionIndex);
    }
}