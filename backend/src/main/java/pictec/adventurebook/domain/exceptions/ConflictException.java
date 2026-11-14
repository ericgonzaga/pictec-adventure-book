package pictec.adventurebook.domain.exceptions;

public class ConflictException extends RuntimeException {

    public ConflictException(String errorMessage) {
        super(errorMessage);
    }
}
