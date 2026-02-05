package com.demo.controller;

import com.demo.dto.TransactionDTO;
import com.demo.service.TransactionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionControllerUnitTests {

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private TransactionController transactionController;

    @Test
    @DisplayName("createTransaction returns CREATED and the saved transaction DTO")
    void createTransactionReturnsCreatedAndSavedTransaction() {
        TransactionDTO input = new TransactionDTO(null, 1L, 2L, "BUY", 10.0, 5.0, new Date());
        TransactionDTO saved = new TransactionDTO(100L, 1L, 2L, "BUY", 10.0, 5.0, new Date());

        when(transactionService.saveTransaction(input)).thenReturn(saved);

        ResponseEntity<TransactionDTO> response = transactionController.createTransaction(input);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(saved);
        verify(transactionService, times(1)).saveTransaction(input);
    }

    @Test
    @DisplayName("getAllTransactions returns OK with list of transactions")
    void getAllTransactionsReturnsOkWithList() {
        TransactionDTO a = new TransactionDTO(1L, 1L, 2L, "BUY", 1.0, 1.0, new Date());
        TransactionDTO b = new TransactionDTO(2L, 2L, 3L, "SELL", 2.0, 2.0, new Date());

        when(transactionService.getAllTransactions()).thenReturn(Arrays.asList(a, b));

        ResponseEntity<List<TransactionDTO>> response = transactionController.getAllTransactions();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsExactlyInAnyOrder(a, b);
        verify(transactionService, times(1)).getAllTransactions();
    }

    @Test
    @DisplayName("getTransactionsByPortfolio returns OK with transactions for given portfolio")
    void getTransactionsByPortfolioReturnsOkWithTransactions() {
        TransactionDTO a = new TransactionDTO(3L, 5L, 2L, "BUY", 3.0, 3.0, new Date());

        when(transactionService.getTransactionsByPortfolio(5L)).thenReturn(Arrays.asList(a));

        ResponseEntity<List<TransactionDTO>> response = transactionController.getTransactionsByPortfolio(5L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsExactly(a);
        verify(transactionService, times(1)).getTransactionsByPortfolio(5L);
    }

    @Test
    @DisplayName("getTransactionById returns OK with the transaction when found")
    void getTransactionByIdReturnsOkWhenFound() {
        TransactionDTO t = new TransactionDTO(20L, 1L, 2L, "SELL", 4.0, 4.0, new Date());

        when(transactionService.getTransactionById(20L)).thenReturn(t);

        ResponseEntity<TransactionDTO> response = transactionController.getTransactionById(20L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(t);
        verify(transactionService, times(1)).getTransactionById(20L);
    }

    @Test
    @DisplayName("getTransactionById returns OK with null body when service returns null")
    void getTransactionByIdReturnsOkWithNullBodyWhenServiceReturnsNull() {
        when(transactionService.getTransactionById(999L)).thenReturn(null);

        ResponseEntity<TransactionDTO> response = transactionController.getTransactionById(999L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNull();
        verify(transactionService, times(1)).getTransactionById(999L);
    }

    @Test
    @DisplayName("deleteTransaction invokes service and returns NO_CONTENT")
    void deleteTransactionInvokesServiceAndReturnsNoContent() {
        doNothing().when(transactionService).deleteTransaction(33L);

        ResponseEntity<Void> response = transactionController.deleteTransaction(33L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(transactionService, times(1)).deleteTransaction(33L);
    }

    @Test
    @DisplayName("deleteTransaction propagates exception thrown by the service")
    void deleteTransactionPropagatesServiceException() {
        doThrow(new RuntimeException("delete failed")).when(transactionService).deleteTransaction(44L);

        assertThrows(RuntimeException.class, () -> transactionController.deleteTransaction(44L));

        verify(transactionService, times(1)).deleteTransaction(44L);
    }
}
