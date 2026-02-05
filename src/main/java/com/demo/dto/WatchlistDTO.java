package com.demo.dto;

import java.util.List;

public class WatchlistDTO {

    private Long id;
    private String name;  // Name of the watchlist
    private List<Long> stockIds;  // List of stock IDs in the watchlist
    private Long userId;  // The user who owns this watchlist

    // Default constructor
    public WatchlistDTO() {
    }

    // Constructor with all fields
    public WatchlistDTO(Long id, String name, List<Long> stockIds, Long userId) {
        this.id = id;
        this.name = name;
        this.stockIds = stockIds;
        this.userId = userId;
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

    public List<Long> getStockIds() {
        return stockIds;
    }

    public void setStockIds(List<Long> stockIds) {
        this.stockIds = stockIds;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "WatchlistDTO{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", stockIds=" + stockIds +
                ", userId=" + userId +
                '}';
    }
}
