package pictec.adventurebook.apis.handlers;

import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import lombok.extern.slf4j.Slf4j;
import pictec.adventurebook.domain.exceptions.ConflictException;
import pictec.adventurebook.domain.exceptions.GameFinishedException;
import pictec.adventurebook.domain.exceptions.InvalidBookException;
import pictec.adventurebook.domain.exceptions.InvalidChoiceException;
import pictec.adventurebook.domain.exceptions.NotFoundException;

@Slf4j
@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    ProblemDetail notFound(NotFoundException ex) {
        return problem(HttpStatus.NOT_FOUND, "Resource not found", ex.getMessage());
    }

    @ExceptionHandler(ConflictException.class)
    ProblemDetail conflict(ConflictException ex) {
        return problem(HttpStatus.CONFLICT, "Conflict", ex.getMessage());
    }

    @ExceptionHandler(GameFinishedException.class)
    ProblemDetail gameFinished(GameFinishedException ex) {
        return problem(HttpStatus.CONFLICT, "Game already finished", ex.getMessage());
    }

    @ExceptionHandler(InvalidBookException.class)
    ProblemDetail invalidBook(InvalidBookException ex) {
        return problem(HttpStatus.BAD_REQUEST, "Invalid book", ex.getMessage());
    }

    @ExceptionHandler(InvalidChoiceException.class)
    ProblemDetail invalidChoice(InvalidChoiceException ex) {
        return problem(HttpStatus.BAD_REQUEST, "Invalid choice", ex.getMessage());
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    ProblemDetail concurrentUpdate(OptimisticLockingFailureException ex) {
        return problem(HttpStatus.CONFLICT, "Concurrent update",
                "The game was modified by another request. Reload and try again.");
    }

    @ExceptionHandler(Exception.class)
    ProblemDetail unexpected(Exception ex) {
        log.error("Unexpected error", ex);
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "Internal error", "Unexpected error");
    }

    private static ProblemDetail problem(HttpStatus status, String title, String detail) {
        var pd = ProblemDetail.forStatusAndDetail(status, detail);
        pd.setTitle(title);
        return pd;
    }
}