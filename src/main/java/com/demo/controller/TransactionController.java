package com.demo.controller;

import com.demo.dto.TransactionDTO;
import com.demo.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.ArrayList;

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
        try {
            // Validate required fields
            if (transactionDTO.getPortfolioId() == null) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            if (transactionDTO.getStockId() == null) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            if (transactionDTO.getTransactionType() == null || 
                (!transactionDTO.getTransactionType().equals("BUY") && !transactionDTO.getTransactionType().equals("SELL"))) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            if (transactionDTO.getAmount() == null || transactionDTO.getAmount() <= 0) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            if (transactionDTO.getPricePerUnit() == null || transactionDTO.getPricePerUnit() <= 0) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            
            TransactionDTO savedTransaction = transactionService.saveTransaction(transactionDTO);
            return new ResponseEntity<>(savedTransaction, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            System.err.println("Transaction creation error: " + e.getMessage());
            if (e.getMessage().contains("Portfolio not found") || e.getMessage().contains("Stock not found")) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            System.err.println("Unexpected error creating transaction: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Get all transactions
    @GetMapping
    public ResponseEntity<List<TransactionDTO>> getAllTransactions() {
        try {
            List<TransactionDTO> transactions = transactionService.getAllTransactions();
            return new ResponseEntity<>(transactions, HttpStatus.OK);
        } catch (Exception e) {
            System.err.println("Error fetching transactions: " + e.getMessage());
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    // Get transactions by portfolio ID
    @GetMapping("/portfolio/{portfolioId}")
    public ResponseEntity<List<TransactionDTO>> getTransactionsByPortfolio(@PathVariable Long portfolioId) {
        try {
            List<TransactionDTO> transactions = transactionService.getTransactionsByPortfolio(portfolioId);
            return new ResponseEntity<>(transactions, HttpStatus.OK);
        } catch (Exception e) {
            System.err.println("Error fetching transactions for portfolio: " + e.getMessage());
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    // Get a specific transaction by ID
    @GetMapping("/{id}")
    public ResponseEntity<TransactionDTO> getTransactionById(@PathVariable Long id) {
        try {
            TransactionDTO transactionDTO = transactionService.getTransactionById(id);
            return new ResponseEntity<>(transactionDTO, HttpStatus.OK);
        } catch (Exception e) {
            System.err.println("Error fetching transaction: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Delete a transaction by ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long id) {
        try {
            transactionService.deleteTransaction(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            System.err.println("Error deleting transaction: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
