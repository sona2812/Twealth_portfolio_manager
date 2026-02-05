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

    /**
     * Saves a portfolio or updates it if it already exists.
     *
     * @param portfolioDTO The portfolio DTO to be saved.
     * @return The saved portfolio DTO.
     */
    @Override
    @Transactional
    public PortfolioDTO savePortfolio(PortfolioDTO portfolioDTO) {
        // Fetch stock IDs
        List<Long> stockIds = portfolioDTO.getStockIds();

        // Fetch stocks by their IDs (assuming they exist)
        List<Stock> stocks = stockRepository.findAllById(stockIds);

        // Create or update Portfolio entity
        Portfolio portfolio = new Portfolio();
        portfolio.setId(portfolioDTO.getId());
        portfolio.setName(portfolioDTO.getName());
        portfolio.setDescription(portfolioDTO.getDescription());
        portfolio.setStocks(stocks);  // Set the stocks related to this portfolio

        // Save portfolio to the repository
        Portfolio savedPortfolio = portfolioRepository.save(portfolio);

        // Calculate total value of the portfolio (sum of stock prices)
        Double totalValue = stocks.stream()
                .mapToDouble(stock -> stock.getCurrentPrice() * stock.getQuantity()) // Assuming stock has price and quantity
                .sum();

        // Return the saved portfolio as a DTO
        return new PortfolioDTO(
                savedPortfolio.getId(),
                savedPortfolio.getName(),
                savedPortfolio.getDescription(),
                totalValue,
                stocks.stream().map(Stock::getId).collect(Collectors.toList()) // Collect stock IDs
        );
    }



    /**
     * Retrieves all portfolios.
     *
     * @return A list of all portfolio DTOs.
     */
    @Override
    public List<PortfolioDTO> getAllPortfolios() {
        List<Portfolio> portfolios = portfolioRepository.findAll();
        return portfolios.stream()
                .map(portfolio -> {
                    Double totalValue = portfolio.getStocks().stream()
                            .mapToDouble(stock -> stock.getCurrentPrice() * stock.getQuantity()) // Calculate total value
                            .sum();
                    return new PortfolioDTO(
                            portfolio.getId(),
                            portfolio.getName(),
                            portfolio.getDescription(),
                            totalValue,
                            portfolio.getStocks().stream().map(Stock::getId).collect(Collectors.toList())
                    );
                })
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a portfolio by its ID.
     *
     * @param portfolioId The portfolio ID.
     * @return The portfolio DTO, or throws an exception if not found.
     */
    @Override
    public PortfolioDTO getPortfolioById(Long portfolioId) {
        Optional<Portfolio> portfolioOpt = portfolioRepository.findById(portfolioId);

        if (portfolioOpt.isEmpty()) {
            throw new RuntimeException("Portfolio not found");
        }

        Portfolio portfolio = portfolioOpt.get();

        // Calculate the total value of the portfolio (sum of stock prices)
        Double totalValue = portfolio.getStocks().stream()
                .mapToDouble(stock -> stock.getCurrentPrice() * stock.getQuantity()) // Assuming Stock has price and quantity
                .sum();

        // Convert the entity to DTO
        return new PortfolioDTO(
                portfolio.getId(),
                portfolio.getName(),
                portfolio.getDescription(),
                totalValue,
                portfolio.getStocks().stream().map(Stock::getId).collect(Collectors.toList())
        );
    }

    /**
     * Deletes a portfolio by its ID.
     *
     * @param portfolioId The portfolio ID.
     */
    @Override
    @Transactional
    public void deletePortfolio(Long portfolioId) {
        if (!portfolioRepository.existsById(portfolioId)) {
            throw new RuntimeException("Portfolio not found");
        }
        portfolioRepository.deleteById(portfolioId);
    }
}
