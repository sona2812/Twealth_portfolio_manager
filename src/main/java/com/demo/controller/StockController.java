package com.demo.controller;

import com.demo.dto.StockDTO;
import com.demo.model.Stock;
import com.demo.service.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

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
        // Convert DTO to entity
        Stock stock = stockDTO.toEntity();

        // Save the stock (create or update)
        Stock savedStock = stockService.saveStock(stock);

        // Convert the saved entity back to DTO and return it
        return new ResponseEntity<>(StockDTO.fromEntity(savedStock), HttpStatus.CREATED);
    }

    // Get all stocks
    @GetMapping
    public ResponseEntity<List<StockDTO>> getAllStocks() {
        List<Stock> stocks = stockService.getAllStocks();
        List<StockDTO> stockDTOs = stocks.stream()
                .map(StockDTO::fromEntity)
                .collect(Collectors.toList());
        return new ResponseEntity<>(stockDTOs, HttpStatus.OK);
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

    // Get total portfolio value
    @GetMapping("/total-value")
    public ResponseEntity<Double> getTotalPortfolioValue() {
        Double totalValue = stockService.getTotalPortfolioValue();
        return new ResponseEntity<>(totalValue, HttpStatus.OK);
    }
}
