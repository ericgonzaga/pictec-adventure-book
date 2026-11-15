package pictec.adventurebook.domain.exceptions;

public class InvalidChoiceException extends RuntimeException {

    public InvalidChoiceException(String errorMessage) {
        super(errorMessage);
    }

}
