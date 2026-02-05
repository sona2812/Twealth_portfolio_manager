package com.demo.dto;

import com.demo.model.Stock;

public class StockDTO {

    private Long id;
    private String symbol;
    private String companyName;
    private Double currentPrice;
    private Integer quantity;
    private Double totalValue;
    private Double changePercent;

    // Default constructor
    public StockDTO() {
    }

    // Constructor to initialize all fields
    public StockDTO(Long id, String symbol, String companyName, Double currentPrice, Integer quantity, Double totalValue, Double changePercent) {
        this.id = id;
        this.symbol = symbol;
        this.companyName = companyName;
        this.currentPrice = currentPrice;
        this.quantity = quantity;
        this.totalValue = totalValue;
        this.changePercent = changePercent;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public Double getChangePercent() {
        return changePercent;
    }

    public void setChangePercent(Double changePercent) {
        this.changePercent = changePercent;
    }

    // Convert a Stock entity to a StockDTO
    public static StockDTO fromEntity(Stock stock) {
        Double totalValue = stock.getCurrentPrice() * stock.getQuantity();
        return new StockDTO(stock.getId(), stock.getSymbol(), stock.getCompanyName(), stock.getCurrentPrice(), stock.getQuantity(), totalValue, 0.0);
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
                "id=" + id +
                ", symbol='" + symbol + '\'' +
                ", companyName='" + companyName + '\'' +
                ", currentPrice=" + currentPrice +
                ", quantity=" + quantity +
                ", totalValue=" + totalValue +
                ", changePercent=" + changePercent +
                '}';
    }
}
