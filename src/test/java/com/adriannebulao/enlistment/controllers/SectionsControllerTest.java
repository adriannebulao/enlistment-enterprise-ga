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

    private SectionsController sectionsController;
    private SubjectRepository subjectRepository;
    private SectionRepository sectionRepository;
    private RoomRepository roomRepository;
    private RedirectAttributes redirectAttrs;

    private final String sectionId = DEFAULT_SECTION_ID;
    private final String subjectId = DEFAULT_SUBJECT_ID;
    private final String roomId = "Psych217";

    @BeforeEach
    void setUp() {
        subjectRepository = mock(SubjectRepository.class);
        sectionRepository = mock(SectionRepository.class);
        roomRepository = mock(RoomRepository.class);
        redirectAttrs = new RedirectAttributesModelMap();

        sectionsController = new SectionsController();
        sectionsController.setSubjectRepo(subjectRepository);
        sectionsController.setSectionRepo(sectionRepository);
        sectionsController.setRoomRepo(roomRepository);
    }



    @Test
    void createSection_save_new_section_to_repository() {
        // Given valid parameter arguments for creating a section
        Subject subject = mock(Subject.class);
        Section section = mock(Section.class);
        Room room = mock(Room.class);

        when(subjectRepository.findById(subjectId)).thenReturn(Optional.of(subject));
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(sectionRepository.save(any(Section.class))).thenReturn(section);

        // When the controller receives the arguments
        String returnPath = sectionsController.createSection(
                sectionId,
                subjectId,
                MTH,
                LocalTime.of(8, 30).toString(),
                LocalTime.of(10, 0).toString(),
                roomId,
                redirectAttrs
        );

        // Then
        assertAll(
                // Verify that it retrieves the entities from the database
                () -> verify(subjectRepository).findById(subjectId),
                () -> verify(roomRepository).findById(roomId),
                // Verify that it saves the section in the database
                () -> verify(sectionRepository).save(any(Section.class)),
                // Verify that the flash attribute is set correctly
                () -> assertEquals("Successfully created new section " + sectionId, redirectAttrs.getFlashAttributes().get("sectionSuccessMessage")),
                // Verify that it returns the correct redirect path
                () -> assertEquals("redirect:sections", returnPath)
        );
    }
}
