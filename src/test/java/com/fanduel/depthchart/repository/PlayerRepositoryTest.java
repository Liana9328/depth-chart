package com.fanduel.depthchart.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.fanduel.depthchart.model.Player;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class PlayerRepositoryTest {

    @Autowired
    private PlayerRepository playerRepository;

    @Test
    void findBySportAndTeamAndNumber_withResult() {
        playerRepository.save(new Player(null, "NFL", "TB", 12, "Tom Brady", null));

        var found = playerRepository.findBySportAndTeamAndNumber("NFL", "TB", 12);

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Tom Brady");
    }

    @Test
    void findBySportAndTeamAndNumber_notFound() {
        playerRepository.save(new Player(null, "NFL", "TB", 12, "Tom Brady", null));

        var found = playerRepository.findBySportAndTeamAndNumber("NFL", "TB", 11);

        assertThat(found).isEmpty();
    }
}

