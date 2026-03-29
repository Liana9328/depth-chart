package com.fanduel.depthchart.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fanduel.depthchart.controller.dto.PlayerPayload;
import com.fanduel.depthchart.controller.dto.PlayerResponse;
import com.fanduel.depthchart.model.DepthChartEntry;
import com.fanduel.depthchart.model.Player;
import com.fanduel.depthchart.repository.DepthChartEntryRepository;
import com.fanduel.depthchart.repository.PlayerRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DepthChartServiceTest {

    private static final String SPORT = "NFL";
    private static final String TEAM = "TB";

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private DepthChartEntryRepository depthChartEntryRepository;

    private DepthChartService depthChartService;

    @BeforeEach
    void setUp() {
        depthChartService =
                new DepthChartService(playerRepository, depthChartEntryRepository,
                        new DepthChartCacheService(depthChartEntryRepository));
    }

    @Test
    void shouldReturnBackupsForGivenPlayer() {
        Player brady = player(1L, 12, "Tom Brady");
        Player gabbert = player(2L, 11, "Blaine Gabbert");
        Player trask = player(3L, 2, "Kyle Trask");

        when(depthChartEntryRepository.findBySportAndTeamAndPositionOrderByDepthAsc(SPORT, TEAM, "QB")).thenReturn(List.of(
                entry(101L, "QB", 0, brady),
                entry(102L, "QB", 1, gabbert),
                entry(103L, "QB", 2, trask)));

        assertThat(depthChartService.getBackups(SPORT, TEAM, "QB", 12))
                .extracting(PlayerResponse::number, PlayerResponse::name)
                .containsExactly(tuple(11, "Blaine Gabbert"), tuple(2, "Kyle Trask"));
    }

    @Test
    void shouldReturnEmptyBackupsWhenPlayerHasNoBackups() {
        Player brady = player(1L, 12, "Tom Brady");
        when(depthChartEntryRepository.findBySportAndTeamAndPositionOrderByDepthAsc(SPORT, TEAM, "QB"))
                .thenReturn(List.of(entry(101L, "QB", 0, brady)));

        assertThat(depthChartService.getBackups(SPORT, TEAM, "QB", 12)).isEmpty();
    }

    @Test
    void shouldReturnEmptyBackupsWhenPlayerIsNotInChart() {
        Player brady = player(1L, 12, "Tom Brady");
        when(depthChartEntryRepository.findBySportAndTeamAndPositionOrderByDepthAsc(SPORT, TEAM, "QB"))
                .thenReturn(List.of(entry(101L, "QB", 0, brady)));

        assertThat(depthChartService.getBackups(SPORT, TEAM, "QB", 99)).isEmpty();
    }

    @Test
    void shouldAppendToEndWhenDepthIsNull() {
        Player existing1 = player(1L, 12, "Tom Brady");
        Player existing2 = player(2L, 11, "Blaine Gabbert");
        Player newPlayer = player(3L, 2, "Kyle Trask");

        when(playerRepository.findBySportAndTeamAndNumber(SPORT, TEAM, 2)).thenReturn(Optional.empty());
        when(playerRepository.save(argThat(p -> p.getNumber().equals(2)))).thenReturn(newPlayer);
        when(depthChartEntryRepository.findBySportAndTeamAndPositionOrderByDepthAsc(SPORT, TEAM, "QB"))
                .thenReturn(List.of(entry(101L, "QB", 0, existing1), entry(102L, "QB", 1, existing2)));

        depthChartService.addPlayerToDepthChart(SPORT, TEAM, "QB", new PlayerPayload(2, "Kyle Trask"), null);

        ArgumentCaptor<List<DepthChartEntry>> captor = ArgumentCaptor.forClass(List.class);
        verify(depthChartEntryRepository).saveAll(captor.capture());
        assertThat(captor.getValue()).extracting(e -> e.getPlayer().getNumber(), DepthChartEntry::getDepth)
                .containsExactly(tuple(12, 0), tuple(11, 1), tuple(2, 2));
    }

    @Test
    void shouldMoveLowerPlayersDownWhenInsertedWithPriority() {
        Player existing1 = player(1L, 12, "Tom Brady");
        Player existing2 = player(2L, 11, "Blaine Gabbert");
        Player newPlayer = player(3L, 2, "Kyle Trask");

        when(playerRepository.findBySportAndTeamAndNumber(SPORT, TEAM, 2)).thenReturn(Optional.empty());
        when(playerRepository.save(argThat(p -> p.getNumber().equals(2)))).thenReturn(newPlayer);
        when(depthChartEntryRepository.findBySportAndTeamAndPositionOrderByDepthAsc(SPORT, TEAM, "QB"))
                .thenReturn(List.of(entry(101L, "QB", 0, existing1), entry(102L, "QB", 1, existing2)));

        depthChartService.addPlayerToDepthChart(SPORT, TEAM, "QB", new PlayerPayload(2, "Kyle Trask"), 1);

        ArgumentCaptor<List<DepthChartEntry>> captor = ArgumentCaptor.forClass(List.class);
        verify(depthChartEntryRepository).saveAll(captor.capture());
        assertThat(captor.getValue()).extracting(e -> e.getPlayer().getNumber(), DepthChartEntry::getDepth)
                .containsExactly(tuple(12, 0), tuple(2, 1), tuple(11, 2));
    }

    @Test
    void shouldReturnEmptyListWhenRemovingPlayerNotInChart() {
        Player brady = player(1L, 12, "Tom Brady");
        when(depthChartEntryRepository.findBySportAndTeamAndPositionOrderByDepthAsc(SPORT, TEAM, "QB"))
                .thenReturn(List.of(entry(101L, "QB", 0, brady)));

        assertThat(depthChartService.removePlayerFromDepthChart(SPORT, TEAM, "QB", 99)).isEmpty();
    }

    @Test
    void shouldReturnRemovedPlayerWhenRemovingExistingPlayer() {
        Player brady = player(1L, 12, "Tom Brady");
        Player gabbert = player(2L, 11, "Blaine Gabbert");
        Player trask = player(3L, 2, "Kyle Trask");

        when(depthChartEntryRepository.findBySportAndTeamAndPositionOrderByDepthAsc(SPORT, TEAM, "QB"))
                .thenReturn(new ArrayList<>(List.of(
                        entry(101L, "QB", 0, brady),
                        entry(102L, "QB", 1, gabbert),
                        entry(103L, "QB", 2, trask))));

        List<PlayerResponse> removed = depthChartService.removePlayerFromDepthChart(SPORT, TEAM, "QB", 11);

        assertThat(removed).extracting(PlayerResponse::number, PlayerResponse::name)
                .containsExactly(tuple(11, "Blaine Gabbert"));
        verify(depthChartEntryRepository).delete(argThat(e -> e.getPlayer().getNumber().equals(11)));
        verify(depthChartEntryRepository).saveAll(any(List.class));
    }

    @Test
    void shouldReturnFullDepthChartGroupedByPosition() {
        Player evans = player(1L, 13, "Mike Evans");
        Player brady = player(2L, 12, "Tom Brady");
        Player godwin = player(3L, 14, "Chris Godwin");
        when(depthChartEntryRepository.findBySportAndTeamOrderByPositionAscDepthAsc(SPORT, TEAM)).thenReturn(List.of(
                entry(101L, "LWR", 0, evans),
                entry(102L, "QB", 0, brady),
                entry(103L, "RWR", 0, godwin)));

        Map<String, List<PlayerResponse>> full = depthChartService.getFullDepthChart(SPORT, TEAM);
        assertThat(full.keySet()).containsExactly("LWR", "QB", "RWR");
        assertThat(full.get("QB")).extracting(PlayerResponse::number).containsExactly(12);
        assertThat(full.get("LWR")).extracting(PlayerResponse::number).containsExactly(13);
        assertThat(full.get("RWR")).extracting(PlayerResponse::number).containsExactly(14);
    }

    private Player player(Long id, Integer number, String name) {
        return new Player(id, SPORT, TEAM, number, name, null);
    }

    private DepthChartEntry entry(Long id, String position, Integer depth, Player player) {
        return new DepthChartEntry(id, SPORT, TEAM, position, depth, player, null);
    }
}
