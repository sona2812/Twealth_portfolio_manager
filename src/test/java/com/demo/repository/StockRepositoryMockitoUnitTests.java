package com.demo.repository;

import com.demo.model.Stock;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StockRepositoryMockitoUnitTests {

    private final StockRepository stockRepository = mock(StockRepository.class);

    @Test
    @DisplayName("save returns the persisted stock with generated id")
    void saveReturnsPersistedStockWithGeneratedId() {
        Stock in = new Stock("AAPL", "Apple Inc", 150.0, 10);

        Stock saved = new Stock("AAPL", "Apple Inc", 150.0, 10);
        saved.setId(100L);

        when(stockRepository.save(in)).thenReturn(saved);

        Stock result = stockRepository.save(in);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(100L);
        assertThat(result.getSymbol()).isEqualTo("AAPL");
        verify(stockRepository, times(1)).save(in);
    }

    @Test
    @DisplayName("findBySymbol returns stock when present")
    void findBySymbolReturnsStockWhenPresent() {
        Stock s = new Stock("GOOG", "Google", 2000.0, 1);
        s.setId(10L);

        when(stockRepository.findBySymbol("GOOG")).thenReturn(Optional.of(s));

        Optional<Stock> fetched = stockRepository.findBySymbol("GOOG");

        assertThat(fetched).isPresent();
        assertThat(fetched.get().getId()).isEqualTo(10L);
        assertThat(fetched.get().getSymbol()).isEqualTo("GOOG");
        verify(stockRepository, times(1)).findBySymbol("GOOG");
    }

    @Test
    @DisplayName("findBySymbol returns empty when unknown symbol")
    void findBySymbolReturnsEmptyWhenUnknownSymbol() {
        when(stockRepository.findBySymbol("UNKNOWN")).thenReturn(Optional.empty());

        Optional<Stock> fetched = stockRepository.findBySymbol("UNKNOWN");

        assertThat(fetched).isNotPresent();
        verify(stockRepository, times(1)).findBySymbol("UNKNOWN");
    }

    @Test
    @DisplayName("findAll returns all stocks in repository")
    void findAllReturnsAllStocks() {
        Stock a = new Stock("X", "X Corp", 10.0, 2);
        a.setId(1L);
        Stock b = new Stock("Y", "Y Corp", 5.0, 3);
        b.setId(2L);

        when(stockRepository.findAll()).thenReturn(Arrays.asList(a, b));

        List<Stock> all = stockRepository.findAll();

        assertThat(all).hasSize(2)
                .extracting(Stock::getId, Stock::getSymbol)
                .containsExactlyInAnyOrder(
                        org.assertj.core.groups.Tuple.tuple(1L, "X"),
                        org.assertj.core.groups.Tuple.tuple(2L, "Y")
                );

        verify(stockRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("deleteById invokes repository delete")
    void deleteByIdInvokesRepositoryDelete() {
        doNothing().when(stockRepository).deleteById(5L);

        stockRepository.deleteById(5L);

        verify(stockRepository, times(1)).deleteById(5L);
    }
}
