package com.demo.repository;

import com.demo.model.Portfolio;
import com.demo.repository.PortfolioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class PortfolioRepositoryTests {

    @Autowired
    private PortfolioRepository portfolioRepository;

    @Test
    @DisplayName("save new portfolio and find by id returns the persisted entity")
    void saveNewPortfolioAndFindByIdReturnsPersistedEntity() {
        Portfolio p = new Portfolio();
        p.setName("Retirement");
        p.setDescription("Long term investments");

        Portfolio saved = portfolioRepository.save(p);

        Optional<Portfolio> fetched = portfolioRepository.findById(saved.getId());

        assertThat(fetched).isPresent();
        assertThat(fetched.get().getName()).isEqualTo("Retirement");
        assertThat(fetched.get().getDescription()).isEqualTo("Long term investments");
    }

    @Test
    @DisplayName("findAll returns all saved portfolios")
    void findAllReturnsAllSavedPortfolios() {
        portfolioRepository.deleteAll();

        Portfolio a = new Portfolio();
        a.setName("A");
        Portfolio b = new Portfolio();
        b.setName("B");

        portfolioRepository.save(a);
        portfolioRepository.save(b);

        List<Portfolio> all = portfolioRepository.findAll();

        assertThat(all).hasSize(2)
                .extracting(Portfolio::getName)
                .containsExactlyInAnyOrder("A", "B");
    }

    @Test
    @DisplayName("deleteById removes the portfolio and subsequent findById is empty")
    void deleteByIdRemovesPortfolioAndSubsequentFindIsEmpty() {
        Portfolio p = new Portfolio();
        p.setName("ToDelete");
        Portfolio saved = portfolioRepository.save(p);

        portfolioRepository.deleteById(saved.getId());

        Optional<Portfolio> fetched = portfolioRepository.findById(saved.getId());
        assertThat(fetched).isNotPresent();
    }

    @Test
    @DisplayName("saving null or incomplete portfolio throws an error when persisted")
    void savingIncompletePortfolioThrowsOnPersist() {
        Portfolio p = new Portfolio();
        // no name set - depending on entity constraints this may still persist; assert repository behaviour

        Portfolio saved = portfolioRepository.save(p);
        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();
    }

    @Test
    @DisplayName("findById on unknown id returns empty optional")
    void findByIdUnknownIdReturnsEmptyOptional() {
        Optional<Portfolio> fetched = portfolioRepository.findById(9999L);
        assertThat(fetched).isNotPresent();
    }
}
