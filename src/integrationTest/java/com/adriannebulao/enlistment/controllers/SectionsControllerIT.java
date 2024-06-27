package com.adriannebulao.enlistment.controllers;

import com.adriannebulao.enlistment.domain.*;
import org.junit.jupiter.api.*;
import org.mockito.Mock;
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
import org.testcontainers.shaded.org.bouncycastle.crypto.ec.ECEncryptor;

import java.time.*;
import java.util.*;
import java.util.concurrent.CountDownLatch;

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

    @Test
    void createSameSection_two_admin() throws Exception {
        // Given
        // Two different admins
        insertTwoAdmins();

        final String roomName = "roomName";
        jdbcTemplate.update("INSERT INTO room (name, capacity) VALUES (?, ?)", roomName, 1);
        jdbcTemplate.update("INSERT INTO subject (subject_id) VALUES (?)", DEFAULT_SUBJECT_ID);

        // When
        // They try to enroll the same subject at the same time
        startCreatingSectionThreads();

        // Then
        // A race condition should occur and no section should be added to the database
        int numSection = jdbcTemplate.queryForObject("SELECT count(*) FROM section WHERE section_id = ?", Integer.class, DEFAULT_SECTION_ID);
        assertEquals(0, numSection);
    }

    private void insertTwoAdmins() {
        List<Object[]> batchArgs = new ArrayList<>();
        for (int i = 10; i < 20; i++) {
            batchArgs.add(new Object[]{i, "firstname", "lastname"});
        }
        jdbcTemplate.batchUpdate("INSERT INTO admin(id, firstname, lastname) VALUES (?, ?, ?)", batchArgs);
    }

    private void startCreatingSectionThreads() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        for (int i = 10; i < 20; i++) {
            final int adminId = i;
            new CreateSectionThread(adminRepository.findById(adminId).orElseThrow(() ->
                    new NoSuchElementException("No admin w/ admin id " + adminId + " found in the DB.")),
                    latch, mockMvc).start();
        }
        latch.countDown();
        Thread.sleep(5000);
    }

    private static class CreateSectionThread extends Thread {
        private final Admin admin;
        private final CountDownLatch latch;
        private final MockMvc mockMvc;
        @Autowired
        private JdbcTemplate jdbcTemplate;

        public CreateSectionThread(Admin admin, CountDownLatch latch, MockMvc mockMvc) {
            this.admin = admin;
            this.latch = latch;
            this.mockMvc = mockMvc;
        }

        @Override
        public void run() {
            try {
                latch.await();
                mockMvc.perform(post("/sections").sessionAttr("admin", admin)
                        .param("sectionId", DEFAULT_SECTION_ID)
                        .param("subjectId", DEFAULT_SUBJECT_ID)
                        .param("days", "MTH")
                        .param("start", "08:30")
                        .param("end", "10:30")
                        .param("roomName", "roomName"));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}