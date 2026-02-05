package com.demo.service;


import com.demo.dto.TransactionDTO;

import java.util.List;


public interface TransactionService {

    // Save a new transaction
    TransactionDTO saveTransaction(TransactionDTO transactionDTO);

    // Get all transactions
    List<TransactionDTO> getAllTransactions();  // Return type changed to TransactionDTO

    // Get transactions by portfolio ID
    List<TransactionDTO> getTransactionsByPortfolio(Long portfolioId);

    // Get a transaction by ID
    TransactionDTO getTransactionById(Long transactionId);

    // Delete a transaction
    void deleteTransaction(Long transactionId);
}

