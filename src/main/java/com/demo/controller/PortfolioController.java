package com.demo.controller;

import com.demo.dto.PortfolioDTO;
import com.demo.service.PortfolioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/portfolios")
public class PortfolioController {

    private final PortfolioService portfolioService;

    @Autowired
    public PortfolioController(PortfolioService portfolioService) {
        this.portfolioService = portfolioService;
    }

    // Create or update a portfolio
    @PostMapping
    public ResponseEntity<PortfolioDTO> createOrUpdatePortfolio(@RequestBody PortfolioDTO portfolioDTO) {
        PortfolioDTO savedPortfolio = portfolioService.savePortfolio(portfolioDTO);
        return new ResponseEntity<>(savedPortfolio, HttpStatus.CREATED);
    }

    // Get all portfolios
    @GetMapping
    public ResponseEntity<List<PortfolioDTO>> getAllPortfolios() {
        List<PortfolioDTO> portfolios = portfolioService.getAllPortfolios();
        return new ResponseEntity<>(portfolios, HttpStatus.OK);
    }

    // Get a portfolio by ID
    @GetMapping("/{id}")
    public ResponseEntity<PortfolioDTO> getPortfolioById(@PathVariable Long id) {
        PortfolioDTO portfolioDTO = portfolioService.getPortfolioById(id);
        return new ResponseEntity<>(portfolioDTO, HttpStatus.OK);
    }

    // Delete a portfolio by ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePortfolio(@PathVariable Long id) {
        portfolioService.deletePortfolio(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
