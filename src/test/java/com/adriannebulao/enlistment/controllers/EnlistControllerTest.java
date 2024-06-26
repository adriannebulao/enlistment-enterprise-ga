package com.adriannebulao.enlistment.controllers;

import com.adriannebulao.enlistment.domain.*;
import org.hibernate.*;
import org.junit.jupiter.api.*;

import jakarta.persistence.*;
import java.util.*;

import static com.adriannebulao.enlistment.domain.TestUtils.*;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;


class EnlistControllerTest {

    @Test
    void enlistOrCancel_enlist_student_in_section() {
        //Given
        // an enlist controller
        EnlistController enlistController = new EnlistController();
        // a student that is in session
        Student student = mock(Student.class);
        // a section id
        final String sectionId = DEFAULT_SECTION_ID;
        // a section repository
        SectionRepository sectionRepository = mock(SectionRepository.class);
        Section section = newDefaultSection();
        when(sectionRepository.findById(sectionId)).thenReturn(Optional.of(section));
        enlistController.setSectionRepo(sectionRepository);
        // a student repository
        StudentRepository studentRepository = mock(StudentRepository.class);
        enlistController.setStudentRepo(studentRepository);
        // an entity manager
        EntityManager entityManager = mock(EntityManager.class);
        Session session = mock(Session.class);
        when(entityManager.unwrap(Session.class)).thenReturn(session);
        enlistController.setEntityManager(entityManager);

        // When we call the enlistOrCancel method with the ENLIST action
        String returnPath = enlistController.enlistOrCancel(student, sectionId, UserAction.ENLIST);

        // Then
        assertAll(
                // fetch the section from the repository using the sectionId
                () -> verify(sectionRepository).findById(sectionId),
                // fetch Hibernate session
                () -> verify(entityManager).unwrap(Session.class),
                // reattach student to Hibernate session
                () -> verify(session).update(student),
                // call enlist method on student and pass the section
                () -> verify(student).enlist(section),
                // save the section
                () -> verify(sectionRepository).save(section),
                //save the student
                () -> verify(studentRepository).save(student),
                // return to the same page but implement post-redirect-get pattern
                () -> assertEquals("redirect:enlist", returnPath)
        );
    }
}
