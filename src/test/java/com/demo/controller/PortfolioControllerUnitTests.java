package com.demo.controller;

import com.demo.dto.PortfolioDTO;
import com.demo.service.PortfolioService;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PortfolioControllerUnitTests {

    @Mock
    private PortfolioService portfolioService;

    @InjectMocks
    private PortfolioController portfolioController;

    @Test
    @DisplayName("createOrUpdatePortfolio returns CREATED and the saved portfolio when service succeeds")
    void createOrUpdatePortfolioReturnsCreatedAndSavedPortfolio() {
        PortfolioDTO input = new PortfolioDTO(null, "MyPortfolio", "desc", 1000.0, Collections.emptyList());
        PortfolioDTO saved = new PortfolioDTO(1L, "MyPortfolio", "desc", 1000.0, Collections.emptyList());

        when(portfolioService.savePortfolio(input)).thenReturn(saved);

        ResponseEntity<PortfolioDTO> response = portfolioController.createOrUpdatePortfolio(input);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(saved);
        verify(portfolioService, times(1)).savePortfolio(input);
    }

    @Test
    @DisplayName("getAllPortfolios returns OK and all portfolios from service")
    void getAllPortfoliosReturnsOkAndAllPortfolios() {
        PortfolioDTO a = new PortfolioDTO(1L, "A", null, null, Collections.emptyList());
        PortfolioDTO b = new PortfolioDTO(2L, "B", null, null, Collections.emptyList());
        List<PortfolioDTO> list = Arrays.asList(a, b);

        when(portfolioService.getAllPortfolios()).thenReturn(list);

        ResponseEntity<List<PortfolioDTO>> response = portfolioController.getAllPortfolios();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsExactlyInAnyOrderElementsOf(list);
        verify(portfolioService, times(1)).getAllPortfolios();
    }

    @Test
    @DisplayName("getPortfolioById returns OK and the portfolio when found")
    void getPortfolioByIdReturnsOkAndPortfolioWhenFound() {
        PortfolioDTO dto = new PortfolioDTO(5L, "Found", "desc", 500.0, Collections.emptyList());

        when(portfolioService.getPortfolioById(5L)).thenReturn(dto);

        ResponseEntity<PortfolioDTO> response = portfolioController.getPortfolioById(5L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(dto);
        verify(portfolioService, times(1)).getPortfolioById(5L);
    }

    @Test
    @DisplayName("deletePortfolio invokes service and returns NO_CONTENT")
    void deletePortfolioInvokesServiceAndReturnsNoContent() {
        doNothing().when(portfolioService).deletePortfolio(7L);

        ResponseEntity<Void> response = portfolioController.deletePortfolio(7L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(portfolioService, times(1)).deletePortfolio(7L);
    }

    @Test
    @DisplayName("createOrUpdatePortfolio returns CREATED with null body when service returns null")
    void createOrUpdatePortfolioReturnsCreatedWithNullBodyWhenServiceReturnsNull() {
        PortfolioDTO input = new PortfolioDTO(null, null, null, null, null);

        when(portfolioService.savePortfolio(input)).thenReturn(null);

        ResponseEntity<PortfolioDTO> response = portfolioController.createOrUpdatePortfolio(input);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNull();
        verify(portfolioService, times(1)).savePortfolio(input);
    }

    @Test
    @DisplayName("getPortfolioById propagates exception thrown by the service")
    void getPortfolioByIdPropagatesServiceException() {
        when(portfolioService.getPortfolioById(999L)).thenThrow(new IllegalArgumentException("not found"));

        assertThrows(IllegalArgumentException.class, () -> portfolioController.getPortfolioById(999L));

        verify(portfolioService, times(1)).getPortfolioById(999L);
    }
}
