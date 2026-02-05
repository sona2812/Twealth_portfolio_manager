package com.demo.service;

import com.demo.dto.PortfolioDTO;

import java.util.List;

public interface PortfolioService {

    // Create or update a portfolio
    PortfolioDTO savePortfolio(PortfolioDTO portfolioDTO);

    // Get all portfolios
    List<PortfolioDTO> getAllPortfolios();

    // Get portfolio by ID
    PortfolioDTO getPortfolioById(Long portfolioId);

    // Delete portfolio by ID
    void deletePortfolio(Long portfolioId);
}


