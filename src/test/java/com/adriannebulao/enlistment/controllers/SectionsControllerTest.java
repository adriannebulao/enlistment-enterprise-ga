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
        SubjectRepository subjectRepository = mock(SubjectRepository.class);
        SectionRepository sectionRepository = mock(SectionRepository.class);
        RoomRepository roomRepository = mock(RoomRepository.class);

        SectionsController sectionsController = new SectionsController();
        sectionsController.setSubjectRepo(subjectRepository);
        sectionsController.setSectionRepo(sectionRepository);
        sectionsController.setRoomRepo(roomRepository);

        final String sectionId = DEFAULT_SECTION_ID;
        final String subjectId = DEFAULT_SUBJECT_ID;
        final String roomId = "Psych217";

        Subject subject = mock(Subject.class);
        Section section = mock(Section.class);
        Room room = mock(Room.class);

        when(subjectRepository.findById(subjectId)).thenReturn(Optional.of(subject));
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(sectionRepository.save(any(Section.class))).thenReturn(section);

        RedirectAttributes redirectAttrs = new RedirectAttributesModelMap();

        // When the controller receives the arguments
        String returnPath = sectionsController.createSection(
                sectionId,
                subjectId,
                MTH,
                LocalTime.of(8, 30).toString(),
                LocalTime.of(10, 0).toString(),
                "Psych217",
                redirectAttrs
        );

        // Then
        assertAll(
                // - it should retrieve the entities from the db, create a new section
                () -> verify(subjectRepository).findById(subjectId),
                () -> verify(roomRepository).findById(roomId),

                // - save the section in the db
                () -> verify(sectionRepository).save(any(Section.class)),

                // - set a flash attribute called "sectionSuccessMessage" with the message "Successfully created new section " + sectionId
                () -> assertEquals("Successfully created new section " + sectionId, redirectAttrs.getFlashAttributes().get("sectionSuccessMessage")),

                // - return the string value "redirect:sections" to redirect to the GET method
                () -> assertEquals("redirect:sections", returnPath)
        );
    }
}
