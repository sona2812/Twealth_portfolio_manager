package com.demo.service;

import com.demo.dto.TransactionDTO;
import com.demo.model.Portfolio;
import com.demo.model.Stock;
import com.demo.model.Transaction;
import com.demo.model.TransactionType;
import com.demo.repository.PortfolioRepository;
import com.demo.repository.StockRepository;
import com.demo.repository.TransactionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplUnitTests {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private PortfolioRepository portfolioRepository;

    @Mock
    private StockRepository stockRepository;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    @Test
    @DisplayName("saveTransaction returns DTO when portfolio and stock exist")
    void saveTransactionReturnsDtoWhenEntitiesExist() {
        Portfolio p = new Portfolio();
        p.setId(1L);
        p.setName("P");

        Stock s = new Stock();
        s.setId(2L);
        s.setSymbol("SYM");

        Date now = new Date();
        TransactionDTO input = new TransactionDTO(null, 1L, 2L, "BUY", 5.0, 10.0, now);

        when(portfolioRepository.findById(1L)).thenReturn(Optional.of(p));
        when(stockRepository.findById(2L)).thenReturn(Optional.of(s));

        Transaction saved = new Transaction();
        saved.setId(99L);
        saved.setPortfolio(p);
        saved.setStock(s);
        saved.setType(TransactionType.BUY);
        saved.setAmount(5.0);
        saved.setPricePerUnit(10.0);
        saved.setTransactionDate(now);

        when(transactionRepository.save(any(Transaction.class))).thenReturn(saved);

        TransactionDTO result = transactionService.saveTransaction(input);

        assertThat(result.getId()).isEqualTo(99L);
        assertThat(result.getPortfolioId()).isEqualTo(1L);
        assertThat(result.getStockId()).isEqualTo(2L);
        assertThat(result.getTransactionType()).isEqualTo("BUY");
        assertThat(result.getAmount()).isEqualTo(5.0);
        assertThat(result.getPricePerUnit()).isEqualTo(10.0);
        assertThat(result.getTransactionDate()).isEqualTo(now);

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository, times(1)).save(captor.capture());
        Transaction captured = captor.getValue();
        assertThat(captured.getPortfolio().getId()).isEqualTo(1L);
        assertThat(captured.getStock().getId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("saveTransaction throws when portfolio missing")
    void saveTransactionThrowsWhenPortfolioMissing() {
        when(portfolioRepository.findById(5L)).thenReturn(Optional.empty());

        TransactionDTO input = new TransactionDTO(null, 5L, 2L, "BUY", 1.0, 1.0, new Date());

        assertThrows(RuntimeException.class, () -> transactionService.saveTransaction(input));

        verify(portfolioRepository, times(1)).findById(5L);
        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("saveTransaction throws when stock missing")
    void saveTransactionThrowsWhenStockMissing() {
        Portfolio p = new Portfolio(); p.setId(3L);
        when(portfolioRepository.findById(3L)).thenReturn(Optional.of(p));
        when(stockRepository.findById(8L)).thenReturn(Optional.empty());

        TransactionDTO input = new TransactionDTO(null, 3L, 8L, "SELL", 2.0, 2.0, new Date());

        assertThrows(RuntimeException.class, () -> transactionService.saveTransaction(input));

        verify(portfolioRepository, times(1)).findById(3L);
        verify(stockRepository, times(1)).findById(8L);
        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("getAllTransactions returns mapped DTOs")
    void getAllTransactionsReturnsMappedDtos() {
        Portfolio p = new Portfolio(); p.setId(1L);
        Stock s = new Stock(); s.setId(2L);
        Transaction t = new Transaction();
        t.setId(11L);
        t.setPortfolio(p);
        t.setStock(s);
        t.setType(TransactionType.SELL);
        t.setAmount(3.0);
        t.setPricePerUnit(4.0);
        t.setTransactionDate(new Date());

        when(transactionRepository.findAll()).thenReturn(Arrays.asList(t));

        List<TransactionDTO> results = transactionService.getAllTransactions();

        assertThat(results).hasSize(1);
        TransactionDTO dto = results.get(0);
        assertThat(dto.getId()).isEqualTo(11L);
        assertThat(dto.getPortfolioId()).isEqualTo(1L);
        assertThat(dto.getStockId()).isEqualTo(2L);
        assertThat(dto.getTransactionType()).isEqualTo("SELL");
    }

    @Test
    @DisplayName("getTransactionsByPortfolio returns DTOs for given portfolio")
    void getTransactionsByPortfolioReturnsDtosForPortfolio() {
        Portfolio p = new Portfolio(); p.setId(7L);
        Stock s = new Stock(); s.setId(9L);
        Transaction t = new Transaction();
        t.setId(21L);
        t.setPortfolio(p);
        t.setStock(s);
        t.setType(TransactionType.BUY);
        t.setAmount(1.0);
        t.setPricePerUnit(2.0);
        t.setTransactionDate(new Date());

        when(transactionRepository.findByPortfolioId(7L)).thenReturn(Arrays.asList(t));

        List<TransactionDTO> results = transaction_service_getTransactionsByPortfolio(7L);

        assertThat(results).hasSize(1);
        TransactionDTO dto = results.get(0);
        assertThat(dto.getId()).isEqualTo(21L);
        assertThat(dto.getPortfolioId()).isEqualTo(7L);
        assertThat(dto.getStockId()).isEqualTo(9L);
        assertThat(dto.getTransactionType()).isEqualTo("BUY");
    }

    private List<TransactionDTO> transaction_service_getTransactionsByPortfolio(Long id) {
        return transactionService.getTransactionsByPortfolio(id);
    }

    @Test
    @DisplayName("getTransactionById returns DTO when found and throws when missing")
    void getTransactionByIdReturnsDtoWhenFoundAndThrowsWhenMissing() {
        Portfolio p = new Portfolio(); p.setId(2L);
        Stock s = new Stock(); s.setId(4L);
        Transaction t = new Transaction();
        t.setId(31L);
        t.setPortfolio(p);
        t.setStock(s);
        t.setType(TransactionType.SELL);
        t.setAmount(6.0);
        t.setPricePerUnit(1.5);
        t.setTransactionDate(new Date());

        when(transactionRepository.findById(31L)).thenReturn(Optional.of(t));

        TransactionDTO dto = transactionService.getTransactionById(31L);
        assertThat(dto.getId()).isEqualTo(31L);

        when(transactionRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> transactionService.getTransactionById(99L));
    }

    @Test
    @DisplayName("deleteTransaction deletes when exists and throws when missing")
    void deleteTransactionDeletesWhenExistsAndThrowsWhenMissing() {
        when(transactionRepository.existsById(50L)).thenReturn(true);
        doNothing().when(transactionRepository).deleteById(50L);

        transactionService.deleteTransaction(50L);

        verify(transactionRepository, times(1)).deleteById(50L);

        when(transactionRepository.existsById(60L)).thenReturn(false);
        assertThrows(RuntimeException.class, () -> transactionService.deleteTransaction(60L));
    }
}
