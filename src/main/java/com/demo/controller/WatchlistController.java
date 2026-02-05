package com.demo.controller;

import com.demo.dto.WatchlistDTO;
import com.demo.service.WatchlistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/watchlists")
public class WatchlistController {

    private final WatchlistService watchlistService;

    @Autowired
    public WatchlistController(WatchlistService watchlistService) {
        this.watchlistService = watchlistService;
    }

    // Create a new watchlist
    @PostMapping
    public ResponseEntity<WatchlistDTO> createWatchlist(@RequestBody WatchlistDTO watchlistDTO) {
        WatchlistDTO createdWatchlist = watchlistService.createWatchlist(watchlistDTO);
        return new ResponseEntity<>(createdWatchlist, HttpStatus.CREATED);
    }

    // Get all watchlists
    @GetMapping
    public ResponseEntity<List<WatchlistDTO>> getAllWatchlists() {
        List<WatchlistDTO> watchlists = watchlistService.getAllWatchlists();
        return new ResponseEntity<>(watchlists, HttpStatus.OK);
    }

    // Get a watchlist by ID
    @GetMapping("/{id}")
    public ResponseEntity<WatchlistDTO> getWatchlistById(@PathVariable Long id) {
        WatchlistDTO watchlistDTO = watchlistService.getWatchlistById(id);
        return new ResponseEntity<>(watchlistDTO, HttpStatus.OK);
    }

    // Delete a watchlist by ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWatchlist(@PathVariable Long id) {
        watchlistService.deleteWatchlist(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    // Update a watchlist by ID
    @PutMapping("/{id}")
    public ResponseEntity<WatchlistDTO> updateWatchlist(@PathVariable Long id, @RequestBody WatchlistDTO watchlistDTO) {
        WatchlistDTO updatedWatchlist = watchlistService.updateWatchlist(id, watchlistDTO);
        return new ResponseEntity<>(updatedWatchlist, HttpStatus.OK);
    }
}
