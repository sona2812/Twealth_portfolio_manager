package com.demo.controller;

import com.demo.dto.TransactionDTO;
import com.demo.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    @Autowired
    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    // Create a new transaction
    @PostMapping
    public ResponseEntity<TransactionDTO> createTransaction(@RequestBody TransactionDTO transactionDTO) {
        TransactionDTO savedTransaction = transactionService.saveTransaction(transactionDTO);
        return new ResponseEntity<>(savedTransaction, HttpStatus.CREATED);
    }

    // Get all transactions
    @GetMapping
    public ResponseEntity<List<TransactionDTO>> getAllTransactions() {
        List<TransactionDTO> transactions = transactionService.getAllTransactions();
        return new ResponseEntity<>(transactions, HttpStatus.OK);
    }

    // Get transactions by portfolio ID
    @GetMapping("/portfolio/{portfolioId}")
    public ResponseEntity<List<TransactionDTO>> getTransactionsByPortfolio(@PathVariable Long portfolioId) {
        List<TransactionDTO> transactions = transactionService.getTransactionsByPortfolio(portfolioId);
        return new ResponseEntity<>(transactions, HttpStatus.OK);
    }

    // Get a specific transaction by ID
    @GetMapping("/{id}")
    public ResponseEntity<TransactionDTO> getTransactionById(@PathVariable Long id) {
        TransactionDTO transactionDTO = transactionService.getTransactionById(id);
        return new ResponseEntity<>(transactionDTO, HttpStatus.OK);
    }

    // Delete a transaction by ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long id) {
        transactionService.deleteTransaction(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
