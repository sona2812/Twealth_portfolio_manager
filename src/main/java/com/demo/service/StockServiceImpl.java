package com.demo.service;

import com.demo.dto.StockDTO;
import com.demo.model.Stock;
import com.demo.repository.StockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class StockServiceImpl implements StockService {

    private final StockRepository stockRepository;
    private final StockApiService stockApiService;

    @Autowired
    public StockServiceImpl(StockRepository stockRepository, StockApiService stockApiService) {
        this.stockRepository = stockRepository;
        this.stockApiService = stockApiService;
    }

    @Override
    public Stock saveStock(Stock stock) {
        return stockRepository.save(stock);
    }

    @Override
    public List<Stock> getAllStocks() {
        return stockRepository.findAll();
    }

    @Override
    public List<StockDTO> getAllStocksWithLivePrices(String apiKey) {
        // Use provided API key or let StockApiService use default from properties
        // Pass null if empty string to allow fallback to properties
        String keyToUse = (apiKey != null && !apiKey.isEmpty()) ? apiKey : null;
        
        // Fetch live prices for all popular stocks (will use properties API key if keyToUse is null)
        List<StockDTO> liveStocks = stockApiService.fetchAllPopularStocks(keyToUse);
        
        // If no live stocks were fetched (no API key available), return database stocks
        if (liveStocks.isEmpty()) {
            return stockRepository.findAll().stream()
                    .map(stock -> {
                        StockDTO dto = StockDTO.fromEntity(stock);
                        dto.setChangePercent(0.0);
                        return dto;
                    })
                    .collect(Collectors.toList());
        }
        
        // Also get any stocks from database and update their prices
        List<Stock> dbStocks = stockRepository.findAll();
        for (Stock dbStock : dbStocks) {
            // Check if we already have this stock in live stocks
            boolean exists = liveStocks.stream()
                    .anyMatch(s -> s.getSymbol().equalsIgnoreCase(dbStock.getSymbol()));
            
            if (!exists) {
                // Fetch live price for this stock (will use properties API key if keyToUse is null)
                StockDTO liveStock = stockApiService.fetchStockQuote(dbStock.getSymbol(), keyToUse);
                if (liveStock != null) {
                    liveStock.setId(dbStock.getId());
                    liveStock.setQuantity(dbStock.getQuantity());
                    liveStock.setTotalValue(liveStock.getCurrentPrice() * liveStock.getQuantity());
                    liveStocks.add(liveStock);
                } else {
                    // If API call fails, use database value with 0 change
                    StockDTO dto = StockDTO.fromEntity(dbStock);
                    dto.setChangePercent(0.0);
                    liveStocks.add(dto);
                }
            } else {
                // Update existing live stock with database quantity
                liveStocks.stream()
                        .filter(s -> s.getSymbol().equalsIgnoreCase(dbStock.getSymbol()))
                        .findFirst()
                        .ifPresent(s -> {
                            s.setId(dbStock.getId());
                            s.setQuantity(dbStock.getQuantity());
                            s.setTotalValue(s.getCurrentPrice() * s.getQuantity());
                        });
            }
        }
        
        return liveStocks;
    }

    @Override
    public Optional<Stock> getStockById(Long id) {
        return stockRepository.findById(id);
    }

    @Override
    public Optional<Stock> getStockBySymbol(String symbol) {
        return stockRepository.findBySymbol(symbol);
    }

    @Override
    public void deleteStockById(Long id) {
        stockRepository.deleteById(id);
    }

    @Override
    public void deleteStockBySymbol(String symbol) {
        stockRepository.findBySymbol(symbol).ifPresent(stock -> stockRepository.delete(stock));
    }

    @Override
    public Double getTotalPortfolioValue() {
        List<Stock> stocks = stockRepository.findAll();
        return stocks.stream().mapToDouble(Stock::getTotalValue).sum();
    }
}
