package pictec.adventurebook.domain.entities;

public class InvalidChoiceException extends RuntimeException {

    public InvalidChoiceException(String errorMessage) {
        super(errorMessage);
    }

}
