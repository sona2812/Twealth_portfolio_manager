package com.demo.service;

import com.demo.dto.PortfolioDTO;
import com.demo.model.Portfolio;
import com.demo.model.Stock;
import com.demo.repository.PortfolioRepository;
import com.demo.repository.StockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PortfolioServiceImpl implements PortfolioService {

    private final PortfolioRepository portfolioRepository;
    private final StockRepository stockRepository;

    @Autowired
    public PortfolioServiceImpl(PortfolioRepository portfolioRepository, StockRepository stockRepository) {
        this.portfolioRepository = portfolioRepository;
        this.stockRepository = stockRepository;
    }

    @Override
    @Transactional
    public PortfolioDTO savePortfolio(PortfolioDTO portfolioDTO) {
        List<Stock> stocks = stockRepository.findAllById(portfolioDTO.getStockIds());

        Portfolio portfolio = new Portfolio();
        portfolio.setId(portfolioDTO.getId());
        portfolio.setName(portfolioDTO.getName());
        portfolio.setDescription(portfolioDTO.getDescription());
        portfolio.setStocks(stocks);

        Portfolio savedPortfolio = portfolioRepository.save(portfolio);

        // Calculate value based on Current Price of the stocks
        Double totalValue = stocks.stream()
                .mapToDouble(stock -> stock.getCurrentPrice() * (stock.getQuantity() != null ? stock.getQuantity() : 0))
                .sum();

        return new PortfolioDTO(
                savedPortfolio.getId(),
                savedPortfolio.getName(),
                savedPortfolio.getDescription(),
                totalValue,
                stocks.stream().map(Stock::getId).collect(Collectors.toList())
        );
    }

    @Override
    public List<PortfolioDTO> getAllPortfolios() {
        return portfolioRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public PortfolioDTO getPortfolioById(Long portfolioId) {
        return portfolioRepository.findById(portfolioId)
                .map(this::convertToDTO)
                .orElseThrow(() -> new RuntimeException("Portfolio not found"));
    }

    private PortfolioDTO convertToDTO(Portfolio portfolio) {
        Double totalValue = portfolio.getStocks().stream()
                .mapToDouble(s -> s.getCurrentPrice() * (s.getQuantity() != null ? s.getQuantity() : 0))
                .sum();
        return new PortfolioDTO(
                portfolio.getId(),
                portfolio.getName(),
                portfolio.getDescription(),
                totalValue,
                portfolio.getStocks().stream().map(Stock::getId).collect(Collectors.toList())
        );
    }

    @Override
    @Transactional
    public void deletePortfolio(Long portfolioId) {
        portfolioRepository.deleteById(portfolioId);
    }
}