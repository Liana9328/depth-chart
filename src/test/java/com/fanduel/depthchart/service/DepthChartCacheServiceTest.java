package com.fanduel.depthchart.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fanduel.depthchart.model.DepthChartEntry;
import com.fanduel.depthchart.repository.DepthChartEntryRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DepthChartCacheServiceTest {

    @Mock
    private DepthChartEntryRepository entryRepository;

    @InjectMocks
    private DepthChartCacheService depthChartCacheService;

    @Test
    void getDepthChart_delegatesToRepository() {
        List<DepthChartEntry> expected = List.of(new DepthChartEntry(), new DepthChartEntry());
        when(entryRepository.findBySportAndTeamAndPositionOrderByDepthAsc("NFL", "TB", "QB")).thenReturn(expected);

        List<DepthChartEntry> result = depthChartCacheService.getDepthChart("NFL", "TB", "QB");

        assertThat(result).isEqualTo(expected);
        verify(entryRepository).findBySportAndTeamAndPositionOrderByDepthAsc("NFL", "TB", "QB");
    }

    @Test
    void getFullChart_delegatesToRepository() {
        List<DepthChartEntry> expected = List.of(new DepthChartEntry());
        when(entryRepository.findBySportAndTeamOrderByPositionAscDepthAsc("NFL", "TB")).thenReturn(expected);

        List<DepthChartEntry> result = depthChartCacheService.getFullChart("NFL", "TB");

        assertThat(result).isEqualTo(expected);
        verify(entryRepository).findBySportAndTeamOrderByPositionAscDepthAsc("NFL", "TB");
    }
}

