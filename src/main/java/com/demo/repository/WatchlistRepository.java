package com.demo.repository;

import com.demo.model.Watchlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WatchlistRepository extends JpaRepository<Watchlist, Long> {

    // Find a watchlist by its ID
    Optional<Watchlist> findById(Long id);

    // Find all watchlists belonging to a specific user
    List<Watchlist> findByUserId(Long userId);

    // Check if a watchlist with a specific ID exists
    boolean existsById(Long id);

    // Delete a watchlist by its ID (handled by JpaRepository)
    void deleteById(Long id);
}

