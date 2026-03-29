package com.fanduel.depthchart.repository;

import com.fanduel.depthchart.model.DepthChartEntry;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepthChartEntryRepository extends JpaRepository<DepthChartEntry, Long> {

    @EntityGraph(attributePaths = "player")
    List<DepthChartEntry> findBySportAndTeamAndPositionOrderByDepthAsc(String sport, String team, String position);

    @EntityGraph(attributePaths = "player")
    List<DepthChartEntry> findBySportAndTeamOrderByPositionAscDepthAsc(String sport, String team);
}
