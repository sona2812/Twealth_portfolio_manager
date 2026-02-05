package com.demo.repository;

import com.demo.model.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {
    // You can add custom queries here if needed
}
