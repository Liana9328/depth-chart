package com.fanduel.depthchart.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class DepthChartControllerIntegrationTest {

    private static final String BASE = "/api/v1/sports/NFL/teams/TB";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void addAndGetFullChart() throws Exception {
        mockMvc.perform(post(BASE + "/depth-charts/QB/players").contentType(MediaType.APPLICATION_JSON)
                .content("{\"player\":{\"number\":12,\"name\":\"Tom Brady\"},\"depth\":0}"))
                .andExpect(status().isCreated());

        mockMvc.perform(get(BASE + "/depth-charts")).andExpect(status().isOk())
                .andExpect(jsonPath("$.QB[0].number").value(12)).andExpect(jsonPath("$.QB[0].name").value("Tom Brady"));
    }

    @Test
    void deleteNotFound_returnsEmptyList() throws Exception {
        mockMvc.perform(delete(BASE + "/depth-charts/QB/players/99")).andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray()).andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void addWithoutDepth_appendsToEnd() throws Exception {
        mockMvc.perform(post(BASE + "/depth-charts/QB/players").contentType(MediaType.APPLICATION_JSON)
                .content("{\"player\":{\"number\":12,\"name\":\"Tom Brady\"},\"depth\":0}"))
                .andExpect(status().isCreated());
        mockMvc.perform(post(BASE + "/depth-charts/QB/players").contentType(MediaType.APPLICATION_JSON)
                .content("{\"player\":{\"number\":11,\"name\":\"Blaine Gabbert\"}}"))
                .andExpect(status().isCreated());

        mockMvc.perform(get(BASE + "/depth-charts")).andExpect(status().isOk())
                .andExpect(jsonPath("$.QB[0].number").value(12))
                .andExpect(jsonPath("$.QB[1].number").value(11));
    }

    @Test
    void addWithPriority_shiftsExistingPlayersDown() throws Exception {
        mockMvc.perform(post(BASE + "/depth-charts/QB/players").contentType(MediaType.APPLICATION_JSON)
                .content("{\"player\":{\"number\":12,\"name\":\"Tom Brady\"},\"depth\":0}"))
                .andExpect(status().isCreated());
        mockMvc.perform(post(BASE + "/depth-charts/QB/players").contentType(MediaType.APPLICATION_JSON)
                .content("{\"player\":{\"number\":11,\"name\":\"Blaine Gabbert\"},\"depth\":1}"))
                .andExpect(status().isCreated());
        mockMvc.perform(post(BASE + "/depth-charts/QB/players").contentType(MediaType.APPLICATION_JSON)
                .content("{\"player\":{\"number\":2,\"name\":\"Kyle Trask\"},\"depth\":1}"))
                .andExpect(status().isCreated());

        mockMvc.perform(get(BASE + "/depth-charts")).andExpect(status().isOk())
                .andExpect(jsonPath("$.QB[0].number").value(12))
                .andExpect(jsonPath("$.QB[1].number").value(2))
                .andExpect(jsonPath("$.QB[2].number").value(11));
    }

    @Test
    void bulkAdd_thenBackups_worksAsExpected() throws Exception {
        mockMvc.perform(post(BASE + "/depth-charts/bulk-add").contentType(MediaType.APPLICATION_JSON).content("""
                [
                  {"position":"QB","player":{"number":12,"name":"Tom Brady"},"depth":0},
                  {"position":"QB","player":{"number":11,"name":"Blaine Gabbert"},"depth":1},
                  {"position":"QB","player":{"number":2,"name":"Kyle Trask"},"depth":2}
                ]
                """)).andExpect(status().isCreated());

        mockMvc.perform(get(BASE + "/depth-charts/QB/players/12/backups")).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].number").value(11))
                .andExpect(jsonPath("$[1].number").value(2));
    }

    @Test
    void backups_whenNoBackups_returnsEmptyList() throws Exception {
        mockMvc.perform(post(BASE + "/depth-charts/QB/players").contentType(MediaType.APPLICATION_JSON)
                .content("{\"player\":{\"number\":12,\"name\":\"Tom Brady\"},\"depth\":0}"))
                .andExpect(status().isCreated());

        mockMvc.perform(get(BASE + "/depth-charts/QB/players/12/backups")).andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray()).andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void backups_whenPlayerNotListed_returnsEmptyList() throws Exception {
        mockMvc.perform(post(BASE + "/depth-charts/QB/players").contentType(MediaType.APPLICATION_JSON)
                .content("{\"player\":{\"number\":12,\"name\":\"Tom Brady\"},\"depth\":0}"))
                .andExpect(status().isCreated());

        mockMvc.perform(get(BASE + "/depth-charts/QB/players/99/backups")).andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray()).andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void removeFound_returnsRemovedPlayerAndReindexes() throws Exception {
        mockMvc.perform(post(BASE + "/depth-charts/QB/players").contentType(MediaType.APPLICATION_JSON)
                .content("{\"player\":{\"number\":12,\"name\":\"Tom Brady\"},\"depth\":0}"))
                .andExpect(status().isCreated());
        mockMvc.perform(post(BASE + "/depth-charts/QB/players").contentType(MediaType.APPLICATION_JSON)
                .content("{\"player\":{\"number\":11,\"name\":\"Blaine Gabbert\"},\"depth\":1}"))
                .andExpect(status().isCreated());
        mockMvc.perform(post(BASE + "/depth-charts/QB/players").contentType(MediaType.APPLICATION_JSON)
                .content("{\"player\":{\"number\":2,\"name\":\"Kyle Trask\"},\"depth\":2}"))
                .andExpect(status().isCreated());

        mockMvc.perform(delete(BASE + "/depth-charts/QB/players/11")).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].number").value(11))
                .andExpect(jsonPath("$[0].name").value("Blaine Gabbert"));

        mockMvc.perform(get(BASE + "/depth-charts")).andExpect(status().isOk())
                .andExpect(jsonPath("$.QB[0].number").value(12))
                .andExpect(jsonPath("$.QB[1].number").value(2));
    }

    @Test
    void addPlayer_invalidPayload_returnsBadRequest() throws Exception {
        mockMvc.perform(post(BASE + "/depth-charts/QB/players").contentType(MediaType.APPLICATION_JSON)
                .content("{\"depth\":0}"))
                .andExpect(status().isBadRequest());
    }
}
