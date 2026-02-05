package com.demo.model;


import jakarta.persistence.*;

import java.util.Date;

@Entity
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "portfolio_id")
    private Portfolio portfolio;  // The portfolio associated with this transaction

    @ManyToOne
    @JoinColumn(name = "stock_id")
    private Stock stock;  // The stock involved in this transaction

    private TransactionType type;  // BUY or SELL

    private Double amount;  // The amount of the stock bought or sold

    private Double pricePerUnit;  // The price per unit of the stock at the time of transaction

    @Temporal(TemporalType.TIMESTAMP)
    private Date transactionDate;  // The date and time the transaction occurred

    // Default constructor
    public Transaction() {
    }

    // Constructor with all fields
    public Transaction(Portfolio portfolio, Stock stock, TransactionType type, Double amount, Double pricePerUnit, Date transactionDate) {
        this.portfolio = portfolio;
        this.stock = stock;
        this.type = type;
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

    public Portfolio getPortfolio() {
        return portfolio;
    }

    public void setPortfolio(Portfolio portfolio) {
        this.portfolio = portfolio;
    }

    public Stock getStock() {
        return stock;
    }

    public void setStock(Stock stock) {
        this.stock = stock;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
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
        return "Transaction{" +
                "id=" + id +
                ", portfolio=" + portfolio.getName() +
                ", stock=" + stock.getSymbol() +
                ", type=" + type +
                ", amount=" + amount +
                ", pricePerUnit=" + pricePerUnit +
                ", transactionDate=" + transactionDate +
                '}';
    }
}
