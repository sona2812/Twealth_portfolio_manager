package com.demo.controller;

import com.demo.dto.StockDTO;
import com.demo.model.Stock;
import com.demo.service.StockService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StockControllerUnitTests {

    @Mock
    private StockService stockService;

    @InjectMocks
    private StockController stockController;

    @Test
    @DisplayName("createOrUpdateStock returns CREATED with the saved stock DTO")
    void createOrUpdateStockReturnsCreatedWithSavedDto() {
        StockDTO dto = new StockDTO("AAPL", "Apple Inc.", 150.0, 2, 300.0);
        Stock toSave = dto.toEntity();
        Stock saved = new Stock("AAPL", "Apple Inc.", 150.0, 2);
        saved.setId(10L);

        when(stockService.saveStock(any(Stock.class))).thenReturn(saved);

        ResponseEntity<StockDTO> response = stockController.createOrUpdateStock(dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).usingRecursiveComparison().isEqualTo(StockDTO.fromEntity(saved));
        verify(stockService, times(1)).saveStock(any(Stock.class));
    }

    @Test
    @DisplayName("getAllStocks returns OK with converted stock DTO list")
    void getAllStocksReturnsOkWithConvertedDtoList() {
        Stock s1 = new Stock("AAA", "Company A", 10.0, 1);
        s1.setId(1L);
        Stock s2 = new Stock("BBB", "Company B", 20.0, 2);
        s2.setId(2L);

        when(stockService.getAllStocks()).thenReturn(Arrays.asList(s1, s2));

        ResponseEntity<List<StockDTO>> response = stockController.getAllStocks();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2)
                .extracting(StockDTO::getSymbol)
                .containsExactlyInAnyOrder("AAA", "BBB");
        verify(stockService, times(1)).getAllStocks();
    }

    @Test
    @DisplayName("getStockById returns OK with DTO when stock exists")
    void getStockByIdReturnsOkWithDtoWhenExists() {
        Stock s = new Stock("MSFT", "Microsoft", 250.0, 1);
        s.setId(5L);

        when(stockService.getStockById(5L)).thenReturn(Optional.of(s));

        ResponseEntity<StockDTO> response = stockController.getStockById(5L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).usingRecursiveComparison().isEqualTo(StockDTO.fromEntity(s));
        verify(stockService, times(1)).getStockById(5L);
    }

    @Test
    @DisplayName("getStockById returns NOT_FOUND when stock is missing")
    void getStockByIdReturnsNotFoundWhenMissing() {
        when(stockService.getStockById(99L)).thenReturn(Optional.empty());

        ResponseEntity<StockDTO> response = stockController.getStockById(99L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNull();
        verify(stockService, times(1)).getStockById(99L);
    }

    @Test
    @DisplayName("getStockBySymbol returns OK with DTO when stock exists")
    void getStockBySymbolReturnsOkWithDtoWhenExists() {
        Stock s = new Stock("TSLA", "Tesla", 600.0, 3);
        s.setId(8L);

        when(stockService.getStockBySymbol("TSLA")).thenReturn(Optional.of(s));

        ResponseEntity<StockDTO> response = stockController.getStockBySymbol("TSLA");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).usingRecursiveComparison().isEqualTo(StockDTO.fromEntity(s));
        verify(stockService, times(1)).getStockBySymbol("TSLA");
    }

    @Test
    @DisplayName("deleteStockById deletes and returns NO_CONTENT when stock exists")
    void deleteStockByIdDeletesAndReturnsNoContentWhenExists() {
        Stock s = new Stock("DEL", "DelCo", 1.0, 1);
        s.setId(12L);

        when(stockService.getStockById(12L)).thenReturn(Optional.of(s));
        doNothing().when(stockService).deleteStockById(12L);

        ResponseEntity<Void> response = stockController.deleteStockById(12L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(stockService, times(1)).getStockById(12L);
        verify(stockService, times(1)).deleteStockById(12L);
    }

    @Test
    @DisplayName("deleteStockById returns NOT_FOUND when stock does not exist")
    void deleteStockByIdReturnsNotFoundWhenMissing() {
        when(stockService.getStockById(777L)).thenReturn(Optional.empty());

        ResponseEntity<Void> response = stockController.deleteStockById(777L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        verify(stockService, times(1)).getStockById(777L);
        verify(stockService, never()).deleteStockById(anyLong());
    }

    @Test
    @DisplayName("deleteStockBySymbol deletes and returns NO_CONTENT when stock exists")
    void deleteStockBySymbolDeletesAndReturnsNoContentWhenExists() {
        Stock s = new Stock("SYM", "Symbolic", 5.0, 1);
        s.setId(20L);

        when(stockService.getStockBySymbol("SYM")).thenReturn(Optional.of(s));
        doNothing().when(stockService).deleteStockBySymbol("SYM");

        ResponseEntity<Void> response = stockController.deleteStockBySymbol("SYM");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(stockService, times(1)).getStockBySymbol("SYM");
        verify(stockService, times(1)).deleteStockBySymbol("SYM");
    }

    @Test
    @DisplayName("getTotalPortfolioValue returns OK with the total value from service")
    void getTotalPortfolioValueReturnsOkWithValue() {
        when(stockService.getTotalPortfolioValue()).thenReturn(12345.67);

        ResponseEntity<Double> response = stockController.getTotalPortfolioValue();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(12345.67);
        verify(stockService, times(1)).getTotalPortfolioValue();
    }

    @Test
    @DisplayName("getStockBySymbol returns NOT_FOUND when symbol is missing")
    void getStockBySymbolReturnsNotFoundWhenMissing() {
        when(stockService.getStockBySymbol("NOPE")).thenReturn(Optional.empty());

        ResponseEntity<StockDTO> response = stockController.getStockBySymbol("NOPE");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNull();
        verify(stockService, times(1)).getStockBySymbol("NOPE");
    }
}
