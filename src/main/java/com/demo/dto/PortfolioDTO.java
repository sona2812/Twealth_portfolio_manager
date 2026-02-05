package com.demo.dto;

import java.util.List;

public class PortfolioDTO {

    private Long id;
    private String name;  // Portfolio name
    private String description;  // Description of the portfolio
    private Double totalValue;  // Total value of the portfolio (calculated from the stocks)
    private List<Long> stockIds;  // List of stock IDs in this portfolio

    // Default constructor
    public PortfolioDTO() {
    }

    // Constructor with all fields
    public PortfolioDTO(Long id, String name, String description, Double totalValue, List<Long> stockIds) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.totalValue = totalValue;
        this.stockIds = stockIds;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getTotalValue() {
        return totalValue;
    }

    public void setTotalValue(Double totalValue) {
        this.totalValue = totalValue;
    }

    public List<Long> getStockIds() {
        return stockIds;
    }

    public void setStockIds(List<Long> stockIds) {
        this.stockIds = stockIds;
    }

    @Override
    public String toString() {
        return "PortfolioDTO{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", totalValue=" + totalValue +
                ", stockIds=" + stockIds +
                '}';
    }
}

