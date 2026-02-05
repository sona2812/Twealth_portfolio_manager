package com.demo.repository;

import com.demo.model.Stock;
import com.demo.model.User;
import com.demo.model.Watchlist;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WatchlistRepositoryMockitoUnitTests {

    private final WatchlistRepository watchlistRepository = mock(WatchlistRepository.class);

    @Test
    @DisplayName("findById returns watchlist when present")
    void findByIdReturnsWatchlistWhenPresent() {
        User u = new User();
        u.setId(1);
        u.setUsername("owner");

        Stock s = new Stock("A", "A", 10.0, 1);
        s.setId(2L);

        Watchlist w = new Watchlist();
        w.setId(5L);
        w.setName("MyWL");
        w.setUser(u);
        w.setStocks(Arrays.asList(s));

        when(watchlistRepository.findById(5L)).thenReturn(Optional.of(w));

        Optional<Watchlist> fetched = watchlistRepository.findById(5L);

        assertThat(fetched).isPresent();
        assertThat(fetched.get().getName()).isEqualTo("MyWL");
        assertThat(fetched.get().getUser().getUsername()).isEqualTo("owner");
        verify(watchlistRepository, times(1)).findById(5L);
    }

    @Test
    @DisplayName("findById returns empty when not found")
    void findByIdReturnsEmptyWhenNotFound() {
        when(watchlistRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Watchlist> fetched = watchlistRepository.findById(99L);

        assertThat(fetched).isNotPresent();
        verify(watchlistRepository, times(1)).findById(99L);
    }

    @Test
    @DisplayName("findByUserId returns all watchlists for user")
    void findByUserIdReturnsAllWatchlistsForUser() {
        User u = new User();
        u.setId(7);
        u.setUsername("bob");

        Watchlist a = new Watchlist();
        a.setId(11L);
        a.setName("A");
        a.setUser(u);

        Watchlist b = new Watchlist();
        b.setId(12L);
        b.setName("B");
        b.setUser(u);

        when(watchlistRepository.findByUserId(7L)).thenReturn(Arrays.asList(a, b));

        List<Watchlist> results = watchlistRepository.findByUserId(7L);

        assertThat(results).hasSize(2)
                .extracting(Watchlist::getName)
                .containsExactlyInAnyOrder("A", "B");

        verify(watchlistRepository, times(1)).findByUserId(7L);
    }

    @Test
    @DisplayName("findByUserId returns empty list when user has none")
    void findByUserIdReturnsEmptyListWhenUserHasNone() {
        when(watchlistRepository.findByUserId(123L)).thenReturn(Collections.emptyList());

        List<Watchlist> results = watchlistRepository.findByUserId(123L);

        assertThat(results).isEmpty();
        verify(watchlistRepository, times(1)).findByUserId(123L);
    }

    @Test
    @DisplayName("existsById returns correct boolean")
    void existsByIdReturnsCorrectBoolean() {
        when(watchlistRepository.existsById(10L)).thenReturn(true);
        when(watchlistRepository.existsById(20L)).thenReturn(false);

        boolean e1 = watchlistRepository.existsById(10L);
        boolean e2 = watchlistRepository.existsById(20L);

        assertThat(e1).isTrue();
        assertThat(e2).isFalse();

        verify(watchlistRepository, times(1)).existsById(10L);
        verify(watchlistRepository, times(1)).existsById(20L);
    }

    @Test
    @DisplayName("deleteById invokes repository delete")
    void deleteByIdInvokesRepositoryDelete() {
        doNothing().when(watchlistRepository).deleteById(9L);

        watchlistRepository.deleteById(9L);

        verify(watchlistRepository, times(1)).deleteById(9L);
    }
}
