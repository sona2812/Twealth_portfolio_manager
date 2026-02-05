package com.demo.service;

import com.demo.dto.PortfolioDTO;
import com.demo.model.Portfolio;
import com.demo.model.Stock;
import com.demo.repository.PortfolioRepository;
import com.demo.repository.StockRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PortfolioServiceImplUnitTests {

    @Mock
    private PortfolioRepository portfolioRepository;

    @Mock
    private StockRepository stockRepository;

    @InjectMocks
    private PortfolioServiceImpl portfolioService;

    @Test
    @DisplayName("savePortfolio computes total value and returns DTO with stock ids")
    void savePortfolioComputesTotalAndReturnsDtoWithStockIds() {
        PortfolioDTO input = new PortfolioDTO(null, "Retire", "desc", null, Arrays.asList(10L, 20L));

        Stock s1 = new Stock("S1", "One", 100.0, 1);
        s1.setId(10L);
        Stock s2 = new Stock("S2", "Two", 50.0, 2);
        s2.setId(20L);

        when(stockRepository.findAllById(input.getStockIds())).thenReturn(Arrays.asList(s1, s2));

        Portfolio savedEntity = new Portfolio();
        savedEntity.setId(7L);
        savedEntity.setName("Retire");
        savedEntity.setDescription("desc");
        savedEntity.setStocks(Arrays.asList(s1, s2));

        when(portfolioRepository.save(any(Portfolio.class))).thenReturn(savedEntity);

        PortfolioDTO result = portfolioService.savePortfolio(input);

        assertThat(result.getId()).isEqualTo(7L);
        assertThat(result.getName()).isEqualTo("Retire");
        assertThat(result.getDescription()).isEqualTo("desc");
        double expectedTotal = 100.0 * 1 + 50.0 * 2;
        assertThat(result.getTotalValue()).isEqualTo(expectedTotal);
        assertThat(result.getStockIds()).containsExactlyInAnyOrder(10L, 20L);

        ArgumentCaptor<Portfolio> captor = ArgumentCaptor.forClass(Portfolio.class);
        verify(portfolioRepository, times(1)).save(captor.capture());
        Portfolio savedArg = captor.getValue();
        assertThat(savedArg.getName()).isEqualTo("Retire");
    }

    @Test
    @DisplayName("savePortfolio handles empty stock list returning zero total")
    void savePortfolioHandlesEmptyStockListReturningZeroTotal() {
        PortfolioDTO input = new PortfolioDTO(null, "Empty", "d", null, Collections.emptyList());

        when(stockRepository.findAllById(Collections.emptyList())).thenReturn(Collections.emptyList());

        Portfolio savedEntity = new Portfolio();
        savedEntity.setId(2L);
        savedEntity.setName("Empty");
        savedEntity.setDescription("d");
        savedEntity.setStocks(Collections.emptyList());

        when(portfolioRepository.save(any(Portfolio.class))).thenReturn(savedEntity);

        PortfolioDTO result = portfolioService.savePortfolio(input);

        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getTotalValue()).isEqualTo(0.0);
        assertThat(result.getStockIds()).isEmpty();
    }

    @Test
    @DisplayName("getAllPortfolios returns DTOs with computed totals")
    void getAllPortfoliosReturnsDtosWithComputedTotals() {
        Stock s1 = new Stock("A", "Aco", 10.0, 1);
        s1.setId(1L);
        Stock s2 = new Stock("B", "Bco", 5.0, 3);
        s2.setId(2L);

        Portfolio p1 = new Portfolio();
        p1.setId(11L);
        p1.setName("P1");
        p1.setDescription("d1");
        p1.setStocks(Arrays.asList(s1));

        Portfolio p2 = new Portfolio();
        p2.setId(12L);
        p2.setName("P2");
        p2.setDescription("d2");
        p2.setStocks(Arrays.asList(s2));

        when(portfolioRepository.findAll()).thenReturn(Arrays.asList(p1, p2));

        List<PortfolioDTO> results = portfolioService.getAllPortfolios();

        assertThat(results).hasSize(2);
        PortfolioDTO r1 = results.stream().filter(r -> r.getId().equals(11L)).findFirst().orElse(null);
        PortfolioDTO r2 = results.stream().filter(r -> r.getId().equals(12L)).findFirst().orElse(null);
        assertThat(r1).isNotNull();
        assertThat(r1.getTotalValue()).isEqualTo(10.0);
        assertThat(r2).isNotNull();
        assertThat(r2.getTotalValue()).isEqualTo(5.0 * 3);
    }

    @Test
    @DisplayName("getPortfolioById throws RuntimeException when not found")
    void getPortfolioByIdThrowsWhenNotFound() {
        when(portfolioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> portfolioService.getPortfolioById(99L));

        verify(portfolioRepository, times(1)).findById(99L);
    }

    @Test
    @DisplayName("getPortfolioById returns DTO when found")
    void getPortfolioByIdReturnsDtoWhenFound() {
        Stock s = new Stock("X", "Xco", 7.5, 4);
        s.setId(3L);
        Portfolio p = new Portfolio();
        p.setId(33L);
        p.setName("Found");
        p.setDescription("desc");
        p.setStocks(Arrays.asList(s));

        when(portfolioRepository.findById(33L)).thenReturn(Optional.of(p));

        PortfolioDTO dto = portfolioService.getPortfolioById(33L);

        assertThat(dto.getId()).isEqualTo(33L);
        assertThat(dto.getTotalValue()).isEqualTo(7.5 * 4);
        assertThat(dto.getStockIds()).containsExactly(3L);
    }

    @Test
    @DisplayName("deletePortfolio throws when portfolio does not exist")
    void deletePortfolioThrowsWhenNotExist() {
        when(portfolioRepository.existsById(100L)).thenReturn(false);

        assertThrows(RuntimeException.class, () -> portfolioService.deletePortfolio(100L));

        verify(portfolioRepository, times(1)).existsById(100L);
    }

    @Test
    @DisplayName("deletePortfolio deletes when portfolio exists")
    void deletePortfolioDeletesWhenExists() {
        when(portfolioRepository.existsById(20L)).thenReturn(true);

        doNothing().when(portfolioRepository).deleteById(20L);

        portfolioService.deletePortfolio(20L);

        verify(portfolioRepository, times(1)).existsById(20L);
        verify(portfolioRepository, times(1)).deleteById(20L);
    }
}
