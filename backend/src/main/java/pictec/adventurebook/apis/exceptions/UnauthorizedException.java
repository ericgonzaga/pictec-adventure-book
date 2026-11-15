package pictec.adventurebook.apis.exceptions;

public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String errorMessage) {
        super(errorMessage);
    }
}
