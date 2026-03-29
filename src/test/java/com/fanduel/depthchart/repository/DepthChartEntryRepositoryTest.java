package com.fanduel.depthchart.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.fanduel.depthchart.model.DepthChartEntry;
import com.fanduel.depthchart.model.Player;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class DepthChartEntryRepositoryTest {

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private DepthChartEntryRepository depthChartEntryRepository;

    @Test
    void findBySportAndTeamAndPositionOrderByDepthAsc_returnsOrderedEntries() {
        Player p1 = playerRepository.save(new Player(null, "NFL", "TB", 12, "Tom Brady", null));
        Player p2 = playerRepository.save(new Player(null, "NFL", "TB", 11, "Blaine Gabbert", null));
        Player p3 = playerRepository.save(new Player(null, "NFL", "TB", 2, "Kyle Trask", null));

        depthChartEntryRepository.save(new DepthChartEntry(null, "NFL", "TB", "QB", 2, p3, null));
        depthChartEntryRepository.save(new DepthChartEntry(null, "NFL", "TB", "QB", 0, p1, null));
        depthChartEntryRepository.save(new DepthChartEntry(null, "NFL", "TB", "QB", 1, p2, null));

        var ordered = depthChartEntryRepository.findBySportAndTeamAndPositionOrderByDepthAsc("NFL", "TB", "QB");

        assertThat(ordered).extracting(DepthChartEntry::getDepth).containsExactly(0, 1, 2);
        assertThat(ordered).extracting(e -> e.getPlayer().getNumber()).containsExactly(12, 11, 2);
    }

    @Test
    void findBySportAndTeamOrderByPositionAscDepthAsc_returnsPositionThenDepthOrdering() {
        Player qb = playerRepository.save(new Player(null, "NFL", "TB", 12, "Tom Brady", null));
        Player lwr1 = playerRepository.save(new Player(null, "NFL", "TB", 13, "Mike Evans", null));
        Player lwr2 = playerRepository.save(new Player(null, "NFL", "TB", 10, "Scott Miller", null));

        depthChartEntryRepository.save(new DepthChartEntry(null, "NFL", "TB", "QB", 0, qb, null));
        depthChartEntryRepository.save(new DepthChartEntry(null, "NFL", "TB", "LWR", 1, lwr2, null));
        depthChartEntryRepository.save(new DepthChartEntry(null, "NFL", "TB", "LWR", 0, lwr1, null));

        var ordered = depthChartEntryRepository.findBySportAndTeamOrderByPositionAscDepthAsc("NFL", "TB");

        assertThat(ordered).extracting(DepthChartEntry::getPosition, DepthChartEntry::getDepth)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple("LWR", 0),
                        org.assertj.core.groups.Tuple.tuple("LWR", 1),
                        org.assertj.core.groups.Tuple.tuple("QB", 0));
    }
}

