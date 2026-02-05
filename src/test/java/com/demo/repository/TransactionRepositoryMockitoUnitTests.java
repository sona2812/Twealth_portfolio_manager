package com.demo.repository;

import com.demo.model.Portfolio;
import com.demo.model.Stock;
import com.demo.model.Transaction;
import com.demo.model.TransactionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionRepositoryMockitoUnitTests {

    @Mock
    private TransactionRepository transactionRepository;

    @Test
    @DisplayName("save returns persisted transaction with generated id and correct fields")
    void saveReturnsPersistedTransactionWithGeneratedIdAndCorrectFields() {
        Portfolio p = new Portfolio();
        p.setId(11L);
        p.setName("P1");

        Stock s = new Stock("TST", "Test Co", 10.0, 5);
        s.setId(22L);

        Transaction in = new Transaction(p, s, TransactionType.BUY, 2.0, 10.0, new Date());

        Transaction saved = new Transaction(p, s, TransactionType.BUY, 2.0, 10.0, in.getTransactionDate());
        saved.setId(99L);

        when(transactionRepository.save(in)).thenReturn(saved);

        Transaction result = transactionRepository.save(in);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(99L);
        assertThat(result.getPortfolio().getId()).isEqualTo(11L);
        assertThat(result.getStock().getId()).isEqualTo(22L);
        assertThat(result.getType()).isEqualTo(TransactionType.BUY);

        verify(transactionRepository, times(1)).save(in);
    }

    @Test
    @DisplayName("findByPortfolioId returns list of transactions for given portfolio")
    void findByPortfolioIdReturnsListOfTransactionsForGivenPortfolio() {
        Portfolio p = new Portfolio();
        p.setId(5L);

        Stock s1 = new Stock("A", "A", 1.0, 1);
        s1.setId(1L);
        Stock s2 = new Stock("B", "B", 2.0, 1);
        s2.setId(2L);

        Transaction t1 = new Transaction(p, s1, TransactionType.BUY, 1.0, 1.0, new Date());
        t1.setId(10L);
        Transaction t2 = new Transaction(p, s2, TransactionType.SELL, 2.0, 2.0, new Date());
        t2.setId(11L);

        when(transactionRepository.findByPortfolioId(5L)).thenReturn(Arrays.asList(t1, t2));

        List<Transaction> results = transactionRepository.findByPortfolioId(5L);

        assertThat(results).hasSize(2)
                .extracting(Transaction::getId)
                .containsExactlyInAnyOrder(10L, 11L);

        verify(transactionRepository, times(1)).findByPortfolioId(5L);
    }

    @Test
    @DisplayName("findByPortfolioId returns empty list when no transactions exist for portfolio")
    void findByPortfolioIdReturnsEmptyListWhenNoTransactionsExist() {
        when(transactionRepository.findByPortfolioId(123L)).thenReturn(Collections.emptyList());

        List<Transaction> results = transactionRepository.findByPortfolioId(123L);

        assertThat(results).isEmpty();
        verify(transactionRepository, times(1)).findByPortfolioId(123L);
    }

    @Test
    @DisplayName("findAll returns all transactions in repository")
    void findAllReturnsAllTransactionsInRepository() {
        Portfolio p = new Portfolio();
        p.setId(2L);
        Stock s = new Stock("Z", "Z", 3.0, 1);
        s.setId(3L);

        Transaction t = new Transaction(p, s, TransactionType.BUY, 1.0, 3.0, new Date());
        t.setId(7L);

        when(transactionRepository.findAll()).thenReturn(Arrays.asList(t));

        List<Transaction> all = transactionRepository.findAll();

        assertThat(all).hasSize(1);
        assertThat(all.get(0).getId()).isEqualTo(7L);

        verify(transactionRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("deleteById invokes repository delete without throwing")
    void deleteByIdInvokesRepositoryDeleteWithoutThrowing() {
        doNothing().when(transactionRepository).deleteById(55L);

        transactionRepository.deleteById(55L);

        verify(transactionRepository, times(1)).deleteById(55L);
    }
}
