package pictec.adventurebook.domain.exceptions;

public class GameFinishedException extends RuntimeException {

    public GameFinishedException(String errorMessage) {
        super(errorMessage);
    }

}
