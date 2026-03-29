package com.fanduel.depthchart.service;

import com.fanduel.depthchart.model.DepthChartEntry;
import com.fanduel.depthchart.repository.DepthChartEntryRepository;
import java.util.List;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

@Component
public class DepthChartCacheService {

    private final DepthChartEntryRepository entryRepository;

    public DepthChartCacheService(DepthChartEntryRepository entryRepository) {
        this.entryRepository = entryRepository;
    }

    @Cacheable(value = "depthChart", key = "#sport + ':' + #team + ':' + #position")
    public List<DepthChartEntry> getDepthChart(String sport, String team, String position) {
        return entryRepository.findBySportAndTeamAndPositionOrderByDepthAsc(sport, team, position);
    }

    @Cacheable(value = "fullChart", key = "#sport + ':' + #team")
    public List<DepthChartEntry> getFullChart(String sport, String team) {
        return entryRepository.findBySportAndTeamOrderByPositionAscDepthAsc(sport, team);
    }
}
