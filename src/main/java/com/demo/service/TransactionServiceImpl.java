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
        // Fetch portfolio and stock using the provided IDs
        Optional<Portfolio> portfolioOpt = portfolioRepository.findById(transactionDTO.getPortfolioId());
        Optional<Stock> stockOpt = stockRepository.findById(transactionDTO.getStockId());

        if (portfolioOpt.isEmpty()) {
            throw new RuntimeException("Portfolio not found");
        }

        if (stockOpt.isEmpty()) {
            throw new RuntimeException("Stock not found");
        }

        // Convert DTO to entity
        Transaction transaction = new Transaction();
        transaction.setId(transactionDTO.getId());
        transaction.setPortfolio(portfolioOpt.get());
        transaction.setStock(stockOpt.get());
        transaction.setType(TransactionType.valueOf(transactionDTO.getTransactionType()));  // Convert string to enum
        transaction.setAmount(transactionDTO.getAmount());
        transaction.setPricePerUnit(transactionDTO.getPricePerUnit());
        transaction.setTransactionDate(transactionDTO.getTransactionDate());

        // Save the transaction in the repository
        Transaction savedTransaction = transactionRepository.save(transaction);

        // Return the saved transaction as a DTO
        return new TransactionDTO(
                savedTransaction.getId(),
                savedTransaction.getPortfolio().getId(),
                savedTransaction.getStock().getId(),
                savedTransaction.getType().name(),
                savedTransaction.getAmount(),
                savedTransaction.getPricePerUnit(),
                savedTransaction.getTransactionDate()
        );
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
