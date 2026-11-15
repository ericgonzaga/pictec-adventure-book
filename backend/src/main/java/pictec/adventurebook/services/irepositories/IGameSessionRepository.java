package pictec.adventurebook.services.irepositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import pictec.adventurebook.domain.entities.GameSession;
import pictec.adventurebook.domain.enums.GameStatus;

public interface IGameSessionRepository extends JpaRepository<GameSession, String> {

    Optional<GameSession> findFirstByUserIdAndStatusOrderByUpdatedAtDesc(String userId, GameStatus status);

    Optional<GameSession> findFirstByUserIdAndBookIdAndStatusOrderByUpdatedAtDesc(
            String userId, String bookId, GameStatus status);

    boolean existsByBookId(String bookId);
}
