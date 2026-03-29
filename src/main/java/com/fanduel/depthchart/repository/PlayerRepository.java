package com.fanduel.depthchart.repository;

import com.fanduel.depthchart.model.Player;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlayerRepository extends JpaRepository<Player, Long> {

    Optional<Player> findBySportAndTeamAndNumber(String sport, String team, Integer number);
}
