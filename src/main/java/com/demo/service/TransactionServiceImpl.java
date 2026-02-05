package com.demo.service;

import com.demo.dto.TransactionDTO;
import com.demo.model.Portfolio;
import com.demo.model.Stock;
import com.demo.model.Transaction;
import com.demo.model.TransactionType;
import com.demo.repository.PortfolioRepository;
import com.demo.repository.StockRepository;
import com.demo.repository.TransactionRepository;
import com.demo.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Date;

@Service
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final PortfolioRepository portfolioRepository;
    private final StockRepository stockRepository;

    @Autowired
    public TransactionServiceImpl(TransactionRepository transactionRepository,
                                  PortfolioRepository portfolioRepository,
                                  StockRepository stockRepository) {
        this.transactionRepository = transactionRepository;
        this.portfolioRepository = portfolioRepository;
        this.stockRepository = stockRepository;
    }

    /**
     * Saves a new transaction in the database.
     *
     * @param transactionDTO The transaction DTO to be saved.
     * @return The saved transaction DTO.
     */
    @Override
    @Transactional
    public TransactionDTO saveTransaction(TransactionDTO transactionDTO) {
        // Fetch portfolio using the provided ID
        Optional<Portfolio> portfolioOpt = portfolioRepository.findById(transactionDTO.getPortfolioId());

        if (portfolioOpt.isEmpty()) {
            throw new RuntimeException("Portfolio not found");
        }

        // Handle stock - try by ID first, then by symbol (for API stocks with temporary IDs)
        Stock stock;
        Optional<Stock> stockOpt = stockRepository.findById(transactionDTO.getStockId());
        
        if (stockOpt.isEmpty()) {
            // If not found by ID, try to find by symbol (for API stocks)
            if (transactionDTO.getStockSymbol() != null && !transactionDTO.getStockSymbol().isEmpty()) {
                Optional<Stock> stockBySymbol = stockRepository.findBySymbol(transactionDTO.getStockSymbol());
                if (stockBySymbol.isPresent()) {
                    stock = stockBySymbol.get();
                } else {
                    // Create new stock if it doesn't exist
                    stock = createNewStock(transactionDTO);
                }
            } else {
                throw new RuntimeException("Stock not found and no symbol provided");
            }
        } else {
            stock = stockOpt.get();
        }

        // Convert DTO to entity
        Transaction transaction = new Transaction();
        transaction.setId(transactionDTO.getId());
        transaction.setPortfolio(portfolioOpt.get());
        transaction.setStock(stock);
        transaction.setType(TransactionType.valueOf(transactionDTO.getTransactionType()));  // Convert string to enum
        transaction.setAmount(transactionDTO.getAmount());
        transaction.setPricePerUnit(transactionDTO.getPricePerUnit());
        
        // Set transaction date - use current date if null is provided
        if (transactionDTO.getTransactionDate() == null) {
            transaction.setTransactionDate(new Date());
        } else {
            transaction.setTransactionDate(transactionDTO.getTransactionDate());
        }

        // Save the transaction in the repository
        Transaction savedTransaction = transactionRepository.save(transaction);

        // Return the saved transaction as a DTO
        return new TransactionDTO(
                savedTransaction.getId(),
                savedTransaction.getPortfolio().getId(),
                savedTransaction.getStock().getId(),
                savedTransaction.getStock().getSymbol(),
                savedTransaction.getType().name(),
                savedTransaction.getAmount(),
                savedTransaction.getPricePerUnit(),
                savedTransaction.getTransactionDate()
        );
    }
    
    /**
     * Helper method to create a new stock from transaction data
     */
    private Stock createNewStock(TransactionDTO transactionDTO) {
        Stock stock = new Stock();
        stock.setSymbol(transactionDTO.getStockSymbol());
        stock.setCompanyName(transactionDTO.getStockSymbol() + " Company"); // Default name
        stock.setCurrentPrice(transactionDTO.getPricePerUnit());
        stock.setQuantity(0); // Use Integer instead of double
        
        try {
            return stockRepository.save(stock);
        } catch (Exception e) {
            System.err.println("Error creating new stock: " + e.getMessage());
            return null;
        }
    }

    /**
     * Retrieves all transactions from the database.
     *
     * @return A list of all transaction DTOs.
     */
    @Override
    public List<TransactionDTO> getAllTransactions() {
        List<Transaction> transactions = transactionRepository.findAll();
        return transactions.stream()
                .map(transaction -> new TransactionDTO(
                        transaction.getId(),
                        transaction.getPortfolio().getId(),
                        transaction.getStock().getId(),
                        transaction.getStock().getSymbol(),
                        transaction.getType().name(),
                        transaction.getAmount(),
                        transaction.getPricePerUnit(),
                        transaction.getTransactionDate()
                ))
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all transactions by a specific portfolio ID.
     *
     * @param portfolioId The portfolio ID to filter transactions.
     * @return A list of transaction DTOs related to the given portfolio.
     */
    @Override
    public List<TransactionDTO> getTransactionsByPortfolio(Long portfolioId) {
        List<Transaction> transactions = transactionRepository.findByPortfolioId(portfolioId);
        return transactions.stream()
                .map(transaction -> new TransactionDTO(
                        transaction.getId(),
                        transaction.getPortfolio().getId(),
                        transaction.getStock().getId(),
                        transaction.getStock().getSymbol(),
                        transaction.getType().name(),
                        transaction.getAmount(),
                        transaction.getPricePerUnit(),
                        transaction.getTransactionDate()
                ))
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a transaction by its ID.
     *
     * @param transactionId The transaction ID.
     * @return The transaction DTO, or an exception if not found.
     */
    @Override
    public TransactionDTO getTransactionById(Long transactionId) {
        Optional<Transaction> transactionOpt = transactionRepository.findById(transactionId);

        if (transactionOpt.isEmpty()) {
            throw new RuntimeException("Transaction not found");
        }

        Transaction transaction = transactionOpt.get();
        return new TransactionDTO(
                transaction.getId(),
                transaction.getPortfolio().getId(),
                transaction.getStock().getId(),
                transaction.getStock().getSymbol(),
                transaction.getType().name(),
                transaction.getAmount(),
                transaction.getPricePerUnit(),
                transaction.getTransactionDate()
        );
    }

    /**
     * Deletes a transaction by its ID.
     *
     * @param transactionId The transaction ID.
     */
    @Override
    @Transactional
    public void deleteTransaction(Long transactionId) {
        if (!transactionRepository.existsById(transactionId)) {
            throw new RuntimeException("Transaction not found");
        }
        transactionRepository.deleteById(transactionId);
    }
}
