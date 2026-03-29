package com.fanduel.depthchart.controller;

import com.fanduel.depthchart.controller.dto.AddPlayerRequest;
import com.fanduel.depthchart.controller.dto.BulkAddRequest;
import com.fanduel.depthchart.controller.dto.PlayerResponse;
import com.fanduel.depthchart.service.DepthChartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/sports/{sport}/teams/{team}/depth-charts")
@Tag(name = "Depth chart")
public class DepthChartController {

    private final DepthChartService depthChartService;

    public DepthChartController(DepthChartService depthChartService) {
        this.depthChartService = depthChartService;
    }

    @PostMapping("/{position}/players")
    @Operation(summary = "Add a player to a position")
    public ResponseEntity<Void> addPlayer(@Parameter(example = "NFL") @PathVariable String sport,
            @Parameter(example = "TB") @PathVariable String team, @PathVariable String position,
            @Valid @RequestBody AddPlayerRequest body) {
        depthChartService.addPlayerToDepthChart(sport, team, position, body.player(), body.depth());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/bulk-add")
    @Operation(summary = "Bulk add players (convenience; sequential adds)")
    public ResponseEntity<Void> bulkAdd(@Parameter(example = "NFL") @PathVariable String sport,
            @Parameter(example = "TB") @PathVariable String team,
            @Valid @RequestBody List<@Valid BulkAddRequest> request) {
        for (BulkAddRequest item : request) {
            depthChartService.addPlayerToDepthChart(sport, team, item.position(), item.player(), item.depth());
        }
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{position}/players/{number}")
    @Operation(summary = "Remove a player from a position")
    public ResponseEntity<List<PlayerResponse>> removePlayer(@Parameter(example = "NFL") @PathVariable String sport,
            @Parameter(example = "TB") @PathVariable String team, @PathVariable String position,
            @Parameter(description = "Player's unique number", example = "12")
            @PathVariable Integer number) {
        return ResponseEntity.ok(depthChartService.removePlayerFromDepthChart(sport, team, position, number));
    }

    @GetMapping("/{position}/players/{number}/backups")
    @Operation(summary = "List backups for a player at a position")
    public List<PlayerResponse> getBackups(@Parameter(example = "NFL") @PathVariable String sport,
            @Parameter(example = "TB") @PathVariable String team, @PathVariable String position,
            @Parameter(description = "Player's unique number", example = "12")
            @PathVariable Integer number) {
        return depthChartService.getBackups(sport, team, position, number);
    }

    @GetMapping
    @Operation(summary = "Full depth chart for the team")
    public Map<String, List<PlayerResponse>> getFullDepthChart(@Parameter(example = "NFL") @PathVariable String sport,
            @Parameter(example = "TB") @PathVariable String team) {
        return depthChartService.getFullDepthChart(sport, team);
    }
}
