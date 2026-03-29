package com.fanduel.depthchart.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;

@Entity
@Table(name = "players", uniqueConstraints = @UniqueConstraint(columnNames = { "sport", "team", "number" }))
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String sport;

    @Column(name = "team", nullable = false)
    private String team;

    @Column(nullable = false)
    private Integer number;

    @Column(nullable = false)
    private String name;

    @Version
    private Integer version;

    public Player() {
    }

    public Player(Long id, String sport, String team, Integer number, String name, Integer version) {
        this.id = id;
        this.sport = sport;
        this.team = team;
        this.number = number;
        this.name = name;
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

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }
}
