package com.adriannebulao.enlistment.controllers;

import com.adriannebulao.enlistment.domain.*;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.web.servlet.mvc.support.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

import static com.adriannebulao.enlistment.domain.Days.MTH;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static com.adriannebulao.enlistment.domain.TestUtils.*;

class SectionsControllerTest {

    @Test
    void createSection_save_new_section_to_repository() {
        // Given the controller, repositories & valid parameter arguments for creating a section
        SectionsController sectionsController = new SectionsController();
        final String sectionId = DEFAULT_SECTION_ID;
        final String subjectId = DEFAULT_SUBJECT_ID;
        final Days days = MTH;
        final String start = "1:00PM";
        final String end = "5:00PM";
        final String roomName = "F612";
        RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);
        // When the controller receives the arguments
        String returnPath = sectionsController.createSection(sectionId, subjectId, days, start, end, roomName, redirectAttributes);
        SectionRepository sectionRepository = mock(SectionRepository.class);
        sectionsController.setSectionRepo(sectionRepository);
        assertEquals("redirect:/enlist", returnPath);
        // Then
        // - it should retrieve the entities from the db, create a new section
        // - save the section in the db
        // - set a flash attribute called "sectionSuccessMessage" with the message "Successfully created new section " + sectionId
        // - return the string value "redirect:sections" to redirect to the GET method

    }
}
