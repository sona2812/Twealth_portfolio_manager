package com.demo.repository;

import com.demo.model.Portfolio;
import com.demo.model.Stock;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PortfolioRepositoryMockitoUnitTests {

    @Mock
    private PortfolioRepository portfolioRepository;

    @Test
    @DisplayName("save returns the persisted portfolio with generated id")
    void saveReturnsPersistedPortfolioWithGeneratedId() {
        Portfolio p = new Portfolio();
        p.setName("UnitTestPortfolio");

        Portfolio saved = new Portfolio();
        saved.setId(42L);
        saved.setName("UnitTestPortfolio");

        when(portfolioRepository.save(p)).thenReturn(saved);

        Portfolio result = portfolioRepository.save(p);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(42L);
        assertThat(result.getName()).isEqualTo("UnitTestPortfolio");
        verify(portfolioRepository, times(1)).save(p);
    }

    @Test
    @DisplayName("findById returns empty optional for unknown id")
    void findByIdReturnsEmptyOptionalForUnknownId() {
        when(portfolioRepository.findById(9999L)).thenReturn(Optional.empty());

        Optional<Portfolio> fetched = portfolioRepository.findById(9999L);

        assertThat(fetched).isNotPresent();
        verify(portfolioRepository, times(1)).findById(9999L);
    }

    @Test
    @DisplayName("findAll returns all saved portfolios")
    void findAllReturnsAllSavedPortfolios() {
        Portfolio a = new Portfolio();
        a.setId(1L);
        a.setName("A");
        Portfolio b = new Portfolio();
        b.setId(2L);
        b.setName("B");

        when(portfolioRepository.findAll()).thenReturn(Arrays.asList(a, b));

        List<Portfolio> all = portfolioRepository.findAll();

        assertThat(all).hasSize(2)
                .extracting(Portfolio::getName)
                .containsExactlyInAnyOrder("A", "B");

        verify(portfolioRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("deleteById removes the portfolio when invoked")
    void deleteByIdRemovesPortfolioWhenInvoked() {
        doNothing().when(portfolioRepository).deleteById(5L);

        portfolioRepository.deleteById(5L);

        verify(portfolioRepository, times(1)).deleteById(5L);
    }

    @Test
    @DisplayName("existsById returns false for non-existing id and true for existing")
    void existsByIdReturnsCorrectBoolean() {
        when(portfolioRepository.existsById(7L)).thenReturn(true);
        when(portfolioRepository.existsById(8L)).thenReturn(false);

        boolean exists7 = portfolioRepository.existsById(7L);
        boolean exists8 = portfolioRepository.existsById(8L);

        assertThat(exists7).isTrue();
        assertThat(exists8).isFalse();

        verify(portfolioRepository, times(1)).existsById(7L);
        verify(portfolioRepository, times(1)).existsById(8L);
    }
}
