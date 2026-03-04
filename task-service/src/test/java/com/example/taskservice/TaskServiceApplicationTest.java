package com.example.taskservice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Trivial Spring Boot tests for demonstration purposes.
 * Uses test H2 datasource configuration, so no external DB is required.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
@DisplayName("Task Service Spring Boot Tests")
class TaskServiceApplicationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Spring context loads")
    void contextLoads() {
        assertThat(mockMvc).isNotNull();
    }

    @Test
    @DisplayName("Health endpoint returns OK")
    void healthEndpointReturnsOk() throws Exception {
        mockMvc.perform(get("/api/tasks/health"))
                .andExpect(status().isOk());
    }
}
