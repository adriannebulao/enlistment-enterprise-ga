package com.adriannebulao.enlistment.controllers;

import com.adriannebulao.enlistment.domain.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.*;
import org.springframework.boot.test.context.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.*;

import java.time.*;
import java.util.*;

import static com.adriannebulao.enlistment.controllers.UserAction.ENLIST;
import static com.adriannebulao.enlistment.domain.Days.MTH;
import static com.adriannebulao.enlistment.domain.TestUtils.*;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@AutoConfigureMockMvc
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@SpringBootTest

class SectionsControllerIT  {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private MockMvc mockMvc;


    @Autowired
    private AdminRepository adminRepository;

    @Container
    private final PostgreSQLContainer container = new PostgreSQLContainer("postgres:14")
            .withDatabaseName("enlistment").withUsername("enlistment").withPassword("enlistment");

    @DynamicPropertySource
    private static void properties(DynamicPropertyRegistry registry){
        registry.add("springdatasource.url", () -> "jdbc:tc:postgresql:14.0-alpine:///enlistment");
    }


    @Test
    void createSection_save_to_db() throws Exception {
        // Given a student record and section record in the database,
        final String roomName = "roomName";
        jdbcTemplate.update("INSERT INTO room (name, capacity) VALUES (?, ?)", roomName, 1);
        jdbcTemplate.update("INSERT INTO subject (subject_id) VALUES (?)", DEFAULT_SUBJECT_ID);
        String sectionId = "COMPSCI2";
        RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);

        // When the SectionController receives a POST request to create a section
        mockMvc.perform(post("/sections").sessionAttr("admin", adminRepository.findById(1).get())
                .param("sectionId", sectionId)
                .param("subjectId", DEFAULT_SUBJECT_ID)
                .param("days", "MTH")
                .param("start", "08:30")
                .param("end", "10:30")
                .param("roomName", roomName));

        // Then a new record in the sections table will be created
        int count = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM section WHERE section_id = ? ", Integer.class, sectionId);
        assertEquals(1, count);
    }
}