package com.demo.controller;

import com.demo.dto.StockDTO;
import com.demo.model.Stock;
import com.demo.service.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.HashMap;

@RestController
@RequestMapping("/stocks")
@CrossOrigin(origins = "*") // Allow all origins for local dev
public class StockController {

    private final StockService stockService;

    @Autowired
    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    @PostMapping
    public ResponseEntity<StockDTO> createOrUpdateStock(@RequestBody StockDTO stockDTO) {
        Stock stock = stockDTO.toEntity();
        Stock savedStock = stockService.saveStock(stock);
        return new ResponseEntity<>(StockDTO.fromEntity(savedStock), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<StockDTO>> getAllStocks(@RequestParam(required = false) String apiKey) {
        List<StockDTO> stockDTOs = stockService.getAllStocksWithLivePrices(apiKey);
        return new ResponseEntity<>(stockDTOs, HttpStatus.OK);
    }

    // NEW: Endpoint for Chart.js in main.js
    @GetMapping("/history/{symbol}/{range}")
    public ResponseEntity<Map<String, Double>> getStockHistory(@PathVariable String symbol,
            @PathVariable String range) {
        // Mocking historical data until a time-series API is integrated
        Map<String, Double> history = new HashMap<>();
        double basePrice = 150.0;
        for (int i = 7; i >= 0; i--) {
            history.put("2024-05-0" + (7 - i), basePrice + (Math.random() * 10));
        }
        return new ResponseEntity<>(history, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<StockDTO> getStockById(@PathVariable Long id) {
        return stockService.getStockById(id)
                .map(value -> new ResponseEntity<>(StockDTO.fromEntity(value), HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStockById(@PathVariable Long id) {
        stockService.deleteStockById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/total-value")
    public ResponseEntity<Double> getTotalPortfolioValue() {
        // Logic should eventually take a User ID
        return new ResponseEntity<>(stockService.getTotalPortfolioValue(), HttpStatus.OK);
    }
}