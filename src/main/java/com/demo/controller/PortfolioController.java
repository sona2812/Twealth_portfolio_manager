package com.demo.controller;

import com.demo.dto.PortfolioDTO;
import com.demo.service.PortfolioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.ArrayList;

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
        try {
            PortfolioDTO savedPortfolio = portfolioService.savePortfolio(portfolioDTO);
            return new ResponseEntity<>(savedPortfolio, HttpStatus.CREATED);
        } catch (Exception e) {
            System.err.println("Error creating portfolio: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Get all portfolios
    @GetMapping
    public ResponseEntity<List<PortfolioDTO>> getAllPortfolios() {
        try {
            List<PortfolioDTO> portfolios = portfolioService.getAllPortfolios();
            return new ResponseEntity<>(portfolios, HttpStatus.OK);
        } catch (Exception e) {
            System.err.println("Error fetching portfolios: " + e.getMessage());
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    // Get a portfolio by ID
    @GetMapping("/{id}")
    public ResponseEntity<PortfolioDTO> getPortfolioById(@PathVariable Long id) {
        try {
            PortfolioDTO portfolioDTO = portfolioService.getPortfolioById(id);
            return new ResponseEntity<>(portfolioDTO, HttpStatus.OK);
        } catch (Exception e) {
            System.err.println("Error fetching portfolio: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Delete a portfolio by ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePortfolio(@PathVariable Long id) {
        try {
            portfolioService.deletePortfolio(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            System.err.println("Error deleting portfolio: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
