package com.demo.service;

import com.demo.dto.WatchlistDTO;

import java.util.List;

public interface WatchlistService {

    WatchlistDTO createWatchlist(WatchlistDTO watchlistDTO);

    List<WatchlistDTO> getAllWatchlists();

    WatchlistDTO getWatchlistById(Long watchlistId);

    void deleteWatchlist(Long watchlistId);

    WatchlistDTO updateWatchlist(Long watchlistId, WatchlistDTO watchlistDTO);
}
