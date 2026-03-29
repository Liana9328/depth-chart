package com.fanduel.depthchart.service;

import com.fanduel.depthchart.controller.dto.PlayerPayload;
import com.fanduel.depthchart.controller.dto.PlayerResponse;
import com.fanduel.depthchart.model.DepthChartEntry;
import com.fanduel.depthchart.model.Player;
import com.fanduel.depthchart.repository.DepthChartEntryRepository;
import com.fanduel.depthchart.repository.PlayerRepository;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DepthChartService {

    private final PlayerRepository playerRepository;
    private final DepthChartEntryRepository depthChartEntryRepository;
    private final DepthChartCacheService depthChartCacheService;

    public DepthChartService(PlayerRepository playerRepository, DepthChartEntryRepository depthChartEntryRepository,
            DepthChartCacheService depthChartCacheService) {
        this.playerRepository = playerRepository;
        this.depthChartEntryRepository = depthChartEntryRepository;
        this.depthChartCacheService = depthChartCacheService;
    }

    @Caching(evict = { @CacheEvict(cacheNames = "depthChart", key = "#sport + ':' + #team + ':' + #position"),
            @CacheEvict(cacheNames = "fullChart", key = "#sport + ':' + #team") })
    @Transactional
    public void addPlayerToDepthChart(String sport, String team, String position, PlayerPayload playerPayload,
            Integer requestedDepth) {
        Player player = findOrCreatePlayer(sport, team, playerPayload.number(), playerPayload.name());

        List<DepthChartEntry> entries = new ArrayList<>(
                depthChartEntryRepository.findBySportAndTeamAndPositionOrderByDepthAsc(sport, team, position));

        Optional<DepthChartEntry> existingEntry = entries.stream()
                .filter(e -> e.getPlayer().getId().equals(player.getId())).findFirst();
        Integer existingDepth = existingEntry.map(DepthChartEntry::getDepth).orElse(null);

        int insertIndex = (requestedDepth == null || requestedDepth >= entries.size()) ? entries.size()
                : Math.max(0, requestedDepth);

        // If the player is already at the requested depth, do nothing
        if (existingDepth != null && existingDepth.equals(insertIndex)) {
            return;
        }

        existingEntry.ifPresent(entries::remove);

        DepthChartEntry entry = existingEntry
                .orElseGet(() -> new DepthChartEntry(null, sport, team, position, 0, player, null));

        entries.add(insertIndex, entry);
        reindexDepthAndSave(entries);
    }

    @Caching(evict = { @CacheEvict(cacheNames = "depthChart", key = "#sport + ':' + #team + ':' + #position"),
            @CacheEvict(cacheNames = "fullChart", key = "#sport + ':' + #team") })
    @Transactional
    public List<PlayerResponse> removePlayerFromDepthChart(String sport, String team, String position, Integer number) {

        List<DepthChartEntry> entries = new ArrayList<>(
                depthChartEntryRepository.findBySportAndTeamAndPositionOrderByDepthAsc(sport, team, position));

        Optional<DepthChartEntry> entryToRemove = entries.stream().filter(e -> e.getPlayer().getNumber().equals(number))
                .findFirst();

        if (entryToRemove.isEmpty()) {
            return List.of();
        }

        Player removedPlayer = entryToRemove.get().getPlayer();
        entries.remove(entryToRemove.get());

        depthChartEntryRepository.delete(entryToRemove.get());
        reindexDepthAndSave(entries);

        return List.of(toPlayerResponse(removedPlayer));
    }

    @Transactional(readOnly = true)
    public List<PlayerResponse> getBackups(String sport, String team, String position, Integer number) {

        List<DepthChartEntry> chart = depthChartCacheService.getDepthChart(sport, team, position);

        int targetIndex = -1;
        for (int i = 0; i < chart.size(); i++) {
            if (chart.get(i).getPlayer().getNumber().equals(number)) {
                targetIndex = i;
                break;
            }
        }

        if (targetIndex == -1 || targetIndex == chart.size() - 1) {
            return List.of();
        }

        return chart.subList(targetIndex + 1, chart.size()).stream().map(DepthChartEntry::getPlayer)
                .map(this::toPlayerResponse).toList();
    }

    @Transactional(readOnly = true)
    public Map<String, List<PlayerResponse>> getFullDepthChart(String sport, String team) {
        List<DepthChartEntry> allEntries = depthChartCacheService.getFullChart(sport, team);

        return allEntries.stream().collect(Collectors.groupingBy(DepthChartEntry::getPosition, LinkedHashMap::new,
                Collectors.mapping(entry -> toPlayerResponse(entry.getPlayer()), Collectors.toList())));
    }

    private Player findOrCreatePlayer(String sport, String team, Integer number, String name) {
        return playerRepository.findBySportAndTeamAndNumber(sport, team, number).map(p -> {
            if (!p.getName().equals(name)) {
                p.setName(name);
                return playerRepository.save(p);
            }
            return p;
        }).orElseGet(() -> playerRepository.save(new Player(null, sport, team, number, name, null)));
    }

    private void reindexDepthAndSave(List<DepthChartEntry> entries) {
        for (int i = 0; i < entries.size(); i++) {
            entries.get(i).setDepth(i);
        }
        depthChartEntryRepository.saveAll(entries);
    }

    private PlayerResponse toPlayerResponse(Player player) {
        return new PlayerResponse(player.getNumber(), player.getName());
    }
}
