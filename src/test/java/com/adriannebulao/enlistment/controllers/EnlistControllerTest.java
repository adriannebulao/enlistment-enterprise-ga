package com.adriannebulao.enlistment.controllers;

import com.adriannebulao.enlistment.domain.*;
import org.hibernate.*;
import org.junit.jupiter.api.*;

import jakarta.persistence.*;
import java.util.*;

import static com.adriannebulao.enlistment.domain.TestUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;


class EnlistControllerTest {

    @Test
    void enlistOrCancel_enlist_student_in_section() {
        // Given
        EnlistController enlistController = new EnlistController();

        Student student = newDefaultStudent();
        final String sectionId = DEFAULT_SECTION_ID;
        // When
        String returnPath = enlistController.enlistOrCancel(student, sectionId, UserAction.ENLIST);
        // Then
        // fetch the section from the repository using the sectionId
        Section section = newDefaultSection();
        SectionRepository sectionRepository = mock(SectionRepository.class);
        when(sectionRepository.findById(sectionId)).thenReturn(Optional.of(section));
        enlistController.setSectionRepo(sectionRepository);
        verify(sectionRepository).findById(sectionId);
        // return to the same page
        assertEquals("redirect:/enlist", returnPath);
    }

}
