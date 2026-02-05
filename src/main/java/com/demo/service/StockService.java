package com.demo.service;

import com.demo.model.Stock;

import java.util.List;
import java.util.Optional;

public interface StockService {

    // Create or update a stock
    Stock saveStock(Stock stock);

    // Get all stocks
    List<Stock> getAllStocks();

    // Get a stock by its ID
    Optional<Stock> getStockById(Long id);

    // Get a stock by its symbol
    Optional<Stock> getStockBySymbol(String symbol);

    // Delete a stock by its ID
    void deleteStockById(Long id);

    // Delete a stock by its symbol
    void deleteStockBySymbol(String symbol);

    // Calculate the total value of all stocks in the portfolio
    Double getTotalPortfolioValue();
}
