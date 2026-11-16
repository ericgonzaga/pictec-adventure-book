package pictec.adventurebook.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import pictec.adventurebook.domain.logic.BookValidator;
import pictec.adventurebook.domain.logic.GameEngine;

@Configuration
public class DomainConfig {

    @Bean
    GameEngine gameEngine(@Value("${books.initial-health}") int initialHealth) {
        return new GameEngine(initialHealth);
    }

    @Bean
    BookValidator bookValidator(@Value("${books.max-consequence-health}") int maxConsequenceHealth) {
        return new BookValidator(maxConsequenceHealth);
    }
}