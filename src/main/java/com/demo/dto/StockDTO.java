package com.demo.dto;

import com.demo.model.Stock;

public class StockDTO {

    private String symbol;
    private String companyName;
    private Double currentPrice;
    private Integer quantity;
    private Double totalValue;

    // Default constructor
    public StockDTO() {
    }

    // Constructor to initialize all fields
    public StockDTO(String symbol, String companyName, Double currentPrice, Integer quantity, Double totalValue) {
        this.symbol = symbol;
        this.companyName = companyName;
        this.currentPrice = currentPrice;
        this.quantity = quantity;
        this.totalValue = totalValue;
    }

    // Getters and Setters
    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public Double getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(Double currentPrice) {
        this.currentPrice = currentPrice;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Double getTotalValue() {
        return totalValue;
    }

    public void setTotalValue(Double totalValue) {
        this.totalValue = totalValue;
    }

    // Convert a Stock entity to a StockDTO
    public static StockDTO fromEntity(Stock stock) {
        Double totalValue = stock.getCurrentPrice() * stock.getQuantity();
        return new StockDTO(stock.getSymbol(), stock.getCompanyName(), stock.getCurrentPrice(), stock.getQuantity(), totalValue);
    }

    // Convert a StockDTO to a Stock entity (optional, for updates)
    public Stock toEntity() {
        Stock stock = new Stock();
        stock.setSymbol(this.symbol);
        stock.setCompanyName(this.companyName);
        stock.setCurrentPrice(this.currentPrice);
        stock.setQuantity(this.quantity);
        return stock;
    }

    @Override
    public String toString() {
        return "StockDTO{" +
                "symbol='" + symbol + '\'' +
                ", companyName='" + companyName + '\'' +
                ", currentPrice=" + currentPrice +
                ", quantity=" + quantity +
                ", totalValue=" + totalValue +
                '}';
    }
}
