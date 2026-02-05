package com.demo.service;

import com.demo.model.Stock;
import com.demo.repository.StockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class StockServiceImpl implements StockService {

    private final StockRepository stockRepository;

    @Autowired
    public StockServiceImpl(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
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
