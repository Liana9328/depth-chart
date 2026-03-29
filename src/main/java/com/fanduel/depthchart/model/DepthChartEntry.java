package com.fanduel.depthchart.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;

@Entity
@Table(name = "depth_chart_entries", uniqueConstraints = @UniqueConstraint(columnNames = { "sport", "team", "position",
        "player_id" }))
public class DepthChartEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String sport;

    @Column(name = "team", nullable = false)
    private String team;

    @Column(nullable = false)
    private String position;

    @Column(name = "depth", nullable = false)
    private Integer depth;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @Version
    private Integer version;

    public DepthChartEntry() {
    }

    public DepthChartEntry(Long id, String sport, String team, String position, Integer depth, Player player,
            Integer version) {
        this.id = id;
        this.sport = sport;
        this.team = team;
        this.position = position;
        this.depth = depth;
        this.player = player;
        this.version = version;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSport() {
        return sport;
    }

    public void setSport(String sport) {
        this.sport = sport;
    }

    public String getTeam() {
        return team;
    }

    public void setTeam(String team) {
        this.team = team;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public Integer getDepth() {
        return depth;
    }

    public void setDepth(Integer depth) {
        this.depth = depth;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }
}
