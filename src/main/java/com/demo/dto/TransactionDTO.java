package com.demo.dto;

import java.util.Date;

public class TransactionDTO {

    private Long id;
    private Long portfolioId;  // Portfolio ID
    private Long stockId;      // Stock ID (can be temporary for API stocks)
    private String stockSymbol; // Stock symbol (for finding/creating API stocks)
    private String transactionType;  // BUY or SELL
    private Double amount;      // Amount of stock
    private Double pricePerUnit;  // Price per unit of the stock
    private Date transactionDate;  // Date of transaction

    // Default constructor
    public TransactionDTO() {
    }

    // Constructor with all fields
    public TransactionDTO(Long id, Long portfolioId, Long stockId, String stockSymbol, String transactionType, Double amount, Double pricePerUnit, Date transactionDate) {
        this.id = id;
        this.portfolioId = portfolioId;
        this.stockId = stockId;
        this.stockSymbol = stockSymbol;
        this.transactionType = transactionType;
        this.amount = amount;
        this.pricePerUnit = pricePerUnit;
        this.transactionDate = transactionDate;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPortfolioId() {
        return portfolioId;
    }

    public void setPortfolioId(Long portfolioId) {
        this.portfolioId = portfolioId;
    }

    public Long getStockId() {
        return stockId;
    }

    public void setStockId(Long stockId) {
        this.stockId = stockId;
    }

    public String getStockSymbol() {
        return stockSymbol;
    }

    public void setStockSymbol(String stockSymbol) {
        this.stockSymbol = stockSymbol;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Double getPricePerUnit() {
        return pricePerUnit;
    }

    public void setPricePerUnit(Double pricePerUnit) {
        this.pricePerUnit = pricePerUnit;
    }

    public Date getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(Date transactionDate) {
        this.transactionDate = transactionDate;
    }

    @Override
    public String toString() {
        return "TransactionDTO{" +
                "id=" + id +
                ", portfolioId=" + portfolioId +
                ", stockId=" + stockId +
                ", stockSymbol='" + stockSymbol + '\'' +
                ", transactionType='" + transactionType + '\'' +
                ", amount=" + amount +
                ", pricePerUnit=" + pricePerUnit +
                ", transactionDate=" + transactionDate +
                '}';
    }
}
