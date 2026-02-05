package com.demo.service;

import com.demo.dto.WatchlistDTO;
import com.demo.model.Stock;
import com.demo.model.User;
import com.demo.model.Watchlist;
import com.demo.repository.StockRepository;
import com.demo.repository.UserRepository;
import com.demo.repository.WatchlistRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WatchlistServiceImplUnitTests {

    @Mock
    private WatchlistRepository watchlistRepository;

    @Mock
    private StockRepository stockRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private WatchlistServiceImpl watchlistService;

    @Test
    @DisplayName("createWatchlist returns DTO when user and stocks exist")
    void createWatchlistReturnsDtoWhenUserAndStocksExist() {
        WatchlistDTO input = new WatchlistDTO(null, "Tech", Arrays.asList(10L, 20L), 3L);

        User user = new User();
        user.setId(3);
        user.setUsername("bob");

        Stock s1 = new Stock(); s1.setId(10L);
        Stock s2 = new Stock(); s2.setId(20L);

        when(userRepository.findById(3L)).thenReturn(Optional.of(user));
        when(stockRepository.findAllById(input.getStockIds())).thenReturn(Arrays.asList(s1, s2));

        Watchlist saved = new Watchlist();
        saved.setId(5L);
        saved.setName("Tech");
        saved.setUser(user);
        saved.setStocks(Arrays.asList(s1, s2));

        when(watchlistRepository.save(any(Watchlist.class))).thenReturn(saved);

        WatchlistDTO result = watchlistService.createWatchlist(input);

        assertThat(result.getId()).isEqualTo(5L);
        assertThat(result.getName()).isEqualTo("Tech");
        assertThat(result.getStockIds()).containsExactlyInAnyOrder(10L, 20L);
        assertThat(result.getUserId()).isEqualTo(3L);

        ArgumentCaptor<Watchlist> captor = ArgumentCaptor.forClass(Watchlist.class);
        verify(watchlistRepository, times(1)).save(captor.capture());
        Watchlist captured = captor.getValue();
        assertThat(captured.getName()).isEqualTo("Tech");
        assertThat(captured.getUser().getId()).isEqualTo(3);
    }

    @Test
    @DisplayName("createWatchlist throws when user missing")
    void createWatchlistThrowsWhenUserMissing() {
        WatchlistDTO input = new WatchlistDTO(null, "NoUser", Collections.emptyList(), 99L);

        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> watchlistService.createWatchlist(input));

        verify(userRepository, times(1)).findById(99L);
        verify(watchlistRepository, never()).save(any());
    }

    @Test
    @DisplayName("getAllWatchlists returns mapped DTOs")
    void getAllWatchlistsReturnsMappedDtos() {
        User user = new User(); user.setId(2); user.setUsername("alice");
        Stock s = new Stock(); s.setId(7L);
        Watchlist w = new Watchlist();
        w.setId(8L);
        w.setName("W1");
        w.setUser(user);
        w.setStocks(Arrays.asList(s));

        when(watchlistRepository.findAll()).thenReturn(Arrays.asList(w));

        List<WatchlistDTO> results = watchlistService.getAllWatchlists();

        assertThat(results).hasSize(1);
        WatchlistDTO dto = results.get(0);
        assertThat(dto.getId()).isEqualTo(8L);
        assertThat(dto.getName()).isEqualTo("W1");
        assertThat(dto.getStockIds()).containsExactly(7L);
        assertThat(dto.getUserId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("getWatchlistById returns DTO when found")
    void getWatchlistByIdReturnsDtoWhenFound() {
        User user = new User(); user.setId(4); user.setUsername("tom");
        Stock s = new Stock(); s.setId(15L);
        Watchlist w = new Watchlist();
        w.setId(12L); w.setName("MyWL"); w.setUser(user); w.setStocks(Arrays.asList(s));

        when(watchlistRepository.findById(12L)).thenReturn(Optional.of(w));

        WatchlistDTO dto = watchlistService.getWatchlistById(12L);

        assertThat(dto.getId()).isEqualTo(12L);
        assertThat(dto.getName()).isEqualTo("MyWL");
        assertThat(dto.getStockIds()).containsExactly(15L);
        assertThat(dto.getUserId()).isEqualTo(4L);
    }

    @Test
    @DisplayName("getWatchlistById throws when not found")
    void getWatchlistByIdThrowsWhenNotFound() {
        when(watchlistRepository.findById(77L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> watchlistService.getWatchlistById(77L));

        verify(watchlistRepository, times(1)).findById(77L);
    }

    @Test
    @DisplayName("deleteWatchlist deletes when exists")
    void deleteWatchlistDeletesWhenExists() {
        when(watchlistRepository.existsById(3L)).thenReturn(true);
        doNothing().when(watchlistRepository).deleteById(3L);

        watchlistService.deleteWatchlist(3L);

        verify(watchlistRepository, times(1)).deleteById(3L);
    }

    @Test
    @DisplayName("deleteWatchlist throws when missing")
    void deleteWatchlistThrowsWhenMissing() {
        when(watchlistRepository.existsById(99L)).thenReturn(false);

        assertThrows(RuntimeException.class, () -> watchlistService.deleteWatchlist(99L));

        verify(watchlistRepository, times(1)).existsById(99L);
    }

    @Test
    @DisplayName("updateWatchlist updates name and stocks and returns DTO when found")
    void updateWatchlistUpdatesAndReturnsDtoWhenFound() {
        Stock new1 = new Stock(); new1.setId(21L);
        Stock new2 = new Stock(); new2.setId(22L);
        Watchlist w = new Watchlist();
        User user = new User(); user.setId(6); user.setUsername("u");
        w.setId(18L); w.setName("Old"); w.setUser(user);
        w.setStocks(Collections.emptyList());

        WatchlistDTO input = new WatchlistDTO(null, "NewName", Arrays.asList(21L,22L), 6L);

        when(watchlistRepository.findById(18L)).thenReturn(Optional.of(w));
        when(stockRepository.findAllById(input.getStockIds())).thenReturn(Arrays.asList(new1, new2));

        Watchlist updated = new Watchlist();
        updated.setId(18L);
        updated.setName("NewName");
        updated.setUser(user);
        updated.setStocks(Arrays.asList(new1, new2));

        when(watchlistRepository.save(any(Watchlist.class))).thenReturn(updated);

        WatchlistDTO result = watchlistService.updateWatchlist(18L, input);

        assertThat(result.getId()).isEqualTo(18L);
        assertThat(result.getName()).isEqualTo("NewName");
        assertThat(result.getStockIds()).containsExactlyInAnyOrder(21L, 22L);
        assertThat(result.getUserId()).isEqualTo(6L);

        verify(watchlistRepository, times(1)).findById(18L);
        verify(watchlistRepository, times(1)).save(any(Watchlist.class));
    }


    @Test
    @DisplayName("updateWatchlist throws when watchlist missing")
    void updateWatchlistThrowsWhenMissing() {
        WatchlistDTO input = new WatchlistDTO(null, "X", Collections.emptyList(), 1L);
        when(watchlistRepository.findById(200L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> watchlistService.updateWatchlist(200L, input));

        verify(watchlistRepository, times(1)).findById(200L);
    }
}
