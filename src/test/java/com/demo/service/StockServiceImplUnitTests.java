package com.demo.service;

import com.demo.model.Stock;
import com.demo.repository.StockRepository;
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
class StockServiceImplUnitTests {

    @Mock
    private StockRepository stockRepository;

    @InjectMocks
    private StockServiceImpl stockService;

    @Test
    @DisplayName("saveStock delegates to repository and returns saved entity")
    void saveStockDelegatesToRepositoryAndReturnsSaved() {
        Stock input = new Stock("A", "Aco", 10.0, 2);
        Stock saved = new Stock("A", "Aco", 10.0, 2);
        saved.setId(5L);

        when(stockRepository.save(input)).thenReturn(saved);

        Stock result = stockService.saveStock(input);

        assertThat(result).isEqualTo(saved);
        verify(stockRepository, times(1)).save(input);
    }

    @Test
    @DisplayName("getAllStocks returns repository values")
    void getAllStocksReturnsRepositoryValues() {
        Stock s1 = new Stock("X", "Xco", 1.0, 1);
        s1.setId(1L);
        Stock s2 = new Stock("Y", "Yco", 2.0, 2);
        s2.setId(2L);

        when(stockRepository.findAll()).thenReturn(Arrays.asList(s1, s2));

        List<Stock> results = stockService.getAllStocks();

        assertThat(results).containsExactlyInAnyOrder(s1, s2);
        verify(stockRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("getStockById returns Optional when present and empty when absent")
    void getStockByIdReturnsOptionalPresentOrEmpty() {
        Stock s = new Stock("Z", "Zco", 3.0, 3);
        s.setId(10L);

        when(stockRepository.findById(10L)).thenReturn(Optional.of(s));
        when(stockRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Stock> present = stockService.getStockById(10L);
        Optional<Stock> missing = stockService.getStockById(99L);

        assertThat(present).isPresent().contains(s);
        assertThat(missing).isNotPresent();
        verify(stockRepository, times(1)).findById(10L);
        verify(stockRepository, times(1)).findById(99L);
    }

    @Test
    @DisplayName("getStockBySymbol returns Optional when repository finds a symbol")
    void getStockBySymbolReturnsOptionalWhenFound() {
        Stock s = new Stock("SYM", "SymCo", 4.0, 1);
        s.setId(7L);

        when(stockRepository.findBySymbol("SYM")).thenReturn(Optional.of(s));

        Optional<Stock> result = stockService.getStockBySymbol("SYM");

        assertThat(result).isPresent().contains(s);
        verify(stockRepository, times(1)).findBySymbol("SYM");
    }

    @Test
    @DisplayName("deleteStockById delegates to repository")
    void deleteStockByIdDelegatesToRepository() {
        doNothing().when(stockRepository).deleteById(4L);

        stockService.deleteStockById(4L);

        verify(stockRepository, times(1)).deleteById(4L);
    }

    @Test
    @DisplayName("deleteStockBySymbol deletes the stock when found")
    void deleteStockBySymbolDeletesWhenFound() {
        Stock s = new Stock("D", "Dco", 5.0, 1);
        s.setId(8L);

        when(stockRepository.findBySymbol("D")).thenReturn(Optional.of(s));

        stockService.deleteStockBySymbol("D");

        verify(stockRepository, times(1)).findBySymbol("D");
        verify(stockRepository, times(1)).delete(s);
    }

    @Test
    @DisplayName("deleteStockBySymbol does nothing when symbol is absent")
    void deleteStockBySymbolDoesNothingWhenAbsent() {
        when(stockRepository.findBySymbol("NO")).thenReturn(Optional.empty());

        stockService.deleteStockBySymbol("NO");

        verify(stockRepository, times(1)).findBySymbol("NO");
        verify(stockRepository, never()).delete(any(Stock.class));
    }

    @Test
    @DisplayName("getTotalPortfolioValue returns 0.0 for empty repository and sums totals when stocks present")
    void getTotalPortfolioValueHandlesEmptyAndSumsValues() {
        when(stockRepository.findAll()).thenReturn(Collections.emptyList());
        Double emptyTotal = stockService.getTotalPortfolioValue();
        assertThat(emptyTotal).isEqualTo(0.0);

        Stock s1 = new Stock("A", "Aco", 10.0, 1);
        s1.setId(1L);
        Stock s2 = new Stock("B", "Bco", 2.5, 4);
        s2.setId(2L);

        when(stockRepository.findAll()).thenReturn(Arrays.asList(s1, s2));

        Double total = stockService.getTotalPortfolioValue();

        double expected = s1.getTotalValue() + s2.getTotalValue();
        assertThat(total).isEqualTo(expected);
        verify(stockRepository, times(2)).findAll();
    }
}
