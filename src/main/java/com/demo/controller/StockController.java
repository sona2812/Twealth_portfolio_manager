package com.demo.controller;

import com.demo.dto.StockDTO;
import com.demo.model.Stock;
import com.demo.service.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.time.LocalDate;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.stream.Collectors;

@RestController
@RequestMapping("/stocks")
public class StockController {

    private final StockService stockService;

    @Autowired
    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    // Create or update a stock
    @PostMapping
    public ResponseEntity<StockDTO> createOrUpdateStock(@RequestBody StockDTO stockDTO) {
        try {
            // Convert DTO to entity
            Stock stock = stockDTO.toEntity();

            // Save the stock (create or update)
            Stock savedStock = stockService.saveStock(stock);

            // Convert the saved entity back to DTO and return it
            return new ResponseEntity<>(StockDTO.fromEntity(savedStock), HttpStatus.CREATED);
        } catch (Exception e) {
            System.err.println("Error creating stock: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Get all stocks with live prices
    @GetMapping
    public ResponseEntity<List<StockDTO>> getAllStocks(@RequestParam(required = false) String apiKey) {
        try {
            List<StockDTO> stockDTOs = stockService.getAllStocksWithLivePrices(apiKey);
            return new ResponseEntity<>(stockDTOs, HttpStatus.OK);
        } catch (Exception e) {
            System.err.println("Error fetching stocks: " + e.getMessage());
            // Return empty list with 200 status instead of 500 error
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    // Get stock by ID
    @GetMapping("/{id}")
    public ResponseEntity<StockDTO> getStockById(@PathVariable Long id) {
        Optional<Stock> stock = stockService.getStockById(id);
        return stock.map(value -> new ResponseEntity<>(StockDTO.fromEntity(value), HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // Get stock by symbol
    @GetMapping("/symbol/{symbol}")
    public ResponseEntity<StockDTO> getStockBySymbol(@PathVariable String symbol) {
        Optional<Stock> stock = stockService.getStockBySymbol(symbol);
        return stock.map(value -> new ResponseEntity<>(StockDTO.fromEntity(value), HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // Delete stock by ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStockById(@PathVariable Long id) {
        if (stockService.getStockById(id).isPresent()) {
            stockService.deleteStockById(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Delete stock by symbol
    @DeleteMapping("/symbol/{symbol}")
    public ResponseEntity<Void> deleteStockBySymbol(@PathVariable String symbol) {
        Optional<Stock> stock = stockService.getStockBySymbol(symbol);
        if (stock.isPresent()) {
            stockService.deleteStockBySymbol(symbol);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Get stock price history
    @GetMapping("/history/{symbol}/{period}")
    public ResponseEntity<Map<String, Double>> getStockHistory(@PathVariable String symbol, @PathVariable String period) {
        try {
            // For now, return mock historical data
            Map<String, Double> history = new HashMap<>();
            
            // Generate mock historical data based on period
            int days = period.equals("1D") ? 1 : period.equals("1W") ? 7 : period.equals("1M") ? 30 : 365;
            
            for (int i = days; i >= 0; i--) {
                LocalDate date = LocalDate.now().minusDays(i);
                // Generate mock price data with some randomness
                double basePrice = 100.0 + (Math.random() * 50);
                double price = basePrice + (Math.sin(i * 0.1) * 10) + (Math.random() * 5 - 2.5);
                history.put(date.toString(), price);
            }
            
            return new ResponseEntity<>(history, HttpStatus.OK);
        } catch (Exception e) {
            System.err.println("Error fetching stock history: " + e.getMessage());
            return new ResponseEntity<>(new HashMap<>(), HttpStatus.OK);
        }
    }

    // Get total portfolio value
    @GetMapping("/total-value")
    public ResponseEntity<Double> getTotalPortfolioValue() {
        Double totalValue = stockService.getTotalPortfolioValue();
        return new ResponseEntity<>(totalValue, HttpStatus.OK);
    }
}
