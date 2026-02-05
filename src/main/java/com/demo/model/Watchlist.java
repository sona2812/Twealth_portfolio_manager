package com.demo.model;

import jakarta.persistence.*;

import java.util.List;

@Entity
public class Watchlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;  // Name of the watchlist (e.g., "Tech Stocks")

    @ManyToMany
    @JoinTable(
            name = "watchlist_stock",
            joinColumns = @JoinColumn(name = "watchlist_id"),
            inverseJoinColumns = @JoinColumn(name = "stock_id"))
    private List<Stock> stocks;  // List of stocks in the watchlist

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;  // The user who owns this watchlist

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

    public List<Stock> getStocks() {
        return stocks;
    }

    public void setStocks(List<Stock> stocks) {
        this.stocks = stocks;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "Watchlist{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", user=" + user +
                ", stocks=" + stocks +
                '}';
    }
}
