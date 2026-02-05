package com.demo.service;

import com.demo.dto.WatchlistDTO;
import com.demo.model.Stock;
import com.demo.model.User;
import com.demo.model.Watchlist;
import com.demo.repository.StockRepository;
import com.demo.repository.UserRepository;
import com.demo.repository.WatchlistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class WatchlistServiceImpl implements WatchlistService {

    private final WatchlistRepository watchlistRepository;
    private final StockRepository stockRepository;
    private final UserRepository userRepository;

    @Autowired
    public WatchlistServiceImpl(WatchlistRepository watchlistRepository,
                                StockRepository stockRepository,
                                UserRepository userRepository) {
        this.watchlistRepository = watchlistRepository;
        this.stockRepository = stockRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public WatchlistDTO createWatchlist(WatchlistDTO watchlistDTO) {
        // Fetch user by userId
        Optional<User> userOpt = userRepository.findById(watchlistDTO.getUserId());
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found");
        }

        User user = userOpt.get();

        // Fetch stocks by their IDs
        List<Stock> stocks = stockRepository.findAllById(watchlistDTO.getStockIds());

        // Create and save the watchlist
        Watchlist watchlist = new Watchlist();
        watchlist.setName(watchlistDTO.getName());
        watchlist.setStocks(stocks);
        watchlist.setUser(user);

        Watchlist savedWatchlist = watchlistRepository.save(watchlist);

        // Return the saved watchlist as DTO
        return new WatchlistDTO(
                savedWatchlist.getId(),
                savedWatchlist.getName(),
                savedWatchlist.getStocks().stream().map(Stock::getId).collect(Collectors.toList()),
                (long) savedWatchlist.getUser().getId()
        );
    }

    @Override
    public List<WatchlistDTO> getAllWatchlists() {
        List<Watchlist> watchlists = watchlistRepository.findAll();
        return watchlists.stream()
                .map(watchlist -> new WatchlistDTO(
                        watchlist.getId(),
                        watchlist.getName(),
                        watchlist.getStocks().stream().map(Stock::getId).collect(Collectors.toList()),
                        (long) watchlist.getUser().getId()))
                .collect(Collectors.toList());
    }

    @Override
    public WatchlistDTO getWatchlistById(Long watchlistId) {
        Optional<Watchlist> watchlistOpt = watchlistRepository.findById(watchlistId);
        if (watchlistOpt.isEmpty()) {
            throw new RuntimeException("Watchlist not found");
        }

        Watchlist watchlist = watchlistOpt.get();
        return new WatchlistDTO(
                watchlist.getId(),
                watchlist.getName(),
                watchlist.getStocks().stream().map(Stock::getId).collect(Collectors.toList()),
                (long) watchlist.getUser().getId()
        );
    }

    @Override
    public void deleteWatchlist(Long watchlistId) {
        if (!watchlistRepository.existsById(watchlistId)) {
            throw new RuntimeException("Watchlist not found");
        }
        watchlistRepository.deleteById(watchlistId);
    }

    @Override
    @Transactional
    public WatchlistDTO updateWatchlist(Long watchlistId, WatchlistDTO watchlistDTO) {
        Optional<Watchlist> watchlistOpt = watchlistRepository.findById(watchlistId);
        if (watchlistOpt.isEmpty()) {
            throw new RuntimeException("Watchlist not found");
        }

        Watchlist watchlist = watchlistOpt.get();
        watchlist.setName(watchlistDTO.getName());

        List<Stock> stocks = stockRepository.findAllById(watchlistDTO.getStockIds());
        watchlist.setStocks(stocks);

        Watchlist updatedWatchlist = watchlistRepository.save(watchlist);

        return new WatchlistDTO(
                updatedWatchlist.getId(),
                updatedWatchlist.getName(),
                updatedWatchlist.getStocks().stream().map(Stock::getId).collect(Collectors.toList()),
                (long) updatedWatchlist.getUser().getId()
        );
    }
}
