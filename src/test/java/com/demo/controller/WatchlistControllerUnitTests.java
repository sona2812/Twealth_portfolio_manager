package com.demo.controller;

import com.demo.dto.WatchlistDTO;
import com.demo.service.WatchlistService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WatchlistControllerUnitTests {

    @Mock
    private WatchlistService watchlistService;

    @InjectMocks
    private WatchlistController watchlistController;

    @Test
    @DisplayName("createWatchlist returns CREATED and the created watchlist")
    void createWatchlistReturnsCreatedAndCreatedWatchlist() {
        WatchlistDTO input = new WatchlistDTO(null, "Tech", Arrays.asList(1L, 2L), 10L);
        WatchlistDTO created = new WatchlistDTO(5L, "Tech", Arrays.asList(1L, 2L), 10L);

        when(watchlistService.createWatchlist(input)).thenReturn(created);

        ResponseEntity<WatchlistDTO> response = watchlistController.createWatchlist(input);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(created);
        verify(watchlistService, times(1)).createWatchlist(input);
    }

    @Test
    @DisplayName("getAllWatchlists returns OK with list of watchlists")
    void getAllWatchlistsReturnsOkWithList() {
        WatchlistDTO a = new WatchlistDTO(1L, "A", Collections.singletonList(1L), 1L);
        WatchlistDTO b = new WatchlistDTO(2L, "B", Collections.singletonList(2L), 2L);

        when(watchlistService.getAllWatchlists()).thenReturn(Arrays.asList(a, b));

        ResponseEntity<List<WatchlistDTO>> response = watchlistController.getAllWatchlists();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsExactlyInAnyOrder(a, b);
        verify(watchlistService, times(1)).getAllWatchlists();
    }

    @Test
    @DisplayName("getWatchlistById returns OK with watchlist when service returns DTO")
    void getWatchlistByIdReturnsOkWhenFound() {
        WatchlistDTO dto = new WatchlistDTO(7L, "MyWL", Collections.emptyList(), 3L);

        when(watchlistService.getWatchlistById(7L)).thenReturn(dto);

        ResponseEntity<WatchlistDTO> response = watchlistController.getWatchlistById(7L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(dto);
        verify(watchlistService, times(1)).getWatchlistById(7L);
    }

    @Test
    @DisplayName("getWatchlistById returns OK with null body when service returns null")
    void getWatchlistByIdReturnsOkWithNullWhenServiceReturnsNull() {
        when(watchlistService.getWatchlistById(99L)).thenReturn(null);

        ResponseEntity<WatchlistDTO> response = watchlistController.getWatchlistById(99L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNull();
        verify(watchlistService, times(1)).getWatchlistById(99L);
    }

    @Test
    @DisplayName("deleteWatchlist invokes service and returns NO_CONTENT")
    void deleteWatchlistInvokesServiceAndReturnsNoContent() {
        doNothing().when(watchlistService).deleteWatchlist(4L);

        ResponseEntity<Void> response = watchlistController.deleteWatchlist(4L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(watchlistService, times(1)).deleteWatchlist(4L);
    }

    @Test
    @DisplayName("updateWatchlist returns OK and the updated watchlist")
    void updateWatchlistReturnsOkAndUpdatedWatchlist() {
        WatchlistDTO input = new WatchlistDTO(null, "Updated", Arrays.asList(3L), 2L);
        WatchlistDTO updated = new WatchlistDTO(8L, "Updated", Arrays.asList(3L), 2L);

        when(watchlistService.updateWatchlist(8L, input)).thenReturn(updated);

        ResponseEntity<WatchlistDTO> response = watchlistController.updateWatchlist(8L, input);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(updated);
        verify(watchlistService, times(1)).updateWatchlist(8L, input);
    }

    @Test
    @DisplayName("updateWatchlist returns OK with null body when service returns null")
    void updateWatchlistReturnsOkWithNullWhenServiceReturnsNull() {
        WatchlistDTO input = new WatchlistDTO(null, "NoChange", Collections.emptyList(), 2L);

        when(watchlistService.updateWatchlist(20L, input)).thenReturn(null);

        ResponseEntity<WatchlistDTO> response = watchlistController.updateWatchlist(20L, input);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNull();
        verify(watchlistService, times(1)).updateWatchlist(20L, input);
    }
}
