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

    private EnlistController enlistController;
    private Student student;
    private final String sectionId = DEFAULT_SECTION_ID;
    private SectionRepository sectionRepository;
    private StudentRepository studentRepository;
    private EntityManager entityManager;
    private Session session;
    private Section section;

    @BeforeEach
    void setUp() {
        enlistController = new EnlistController();
        student = mock(Student.class);
        section = newDefaultSection();

        sectionRepository = mock(SectionRepository.class);
        when(sectionRepository.findById(sectionId)).thenReturn(Optional.of(section));
        enlistController.setSectionRepo(sectionRepository);

        studentRepository = mock(StudentRepository.class);
        enlistController.setStudentRepo(studentRepository);

        entityManager = mock(EntityManager.class);
        session = mock(Session.class);
        when(entityManager.unwrap(Session.class)).thenReturn(session);
        enlistController.setEntityManager(entityManager);
    }

    @Test
    void enlistOrCancel_enlist_student_in_section() {
        // When we call the enlistOrCancel method with the ENLIST action
        String returnPath = enlistController.enlistOrCancel(student, sectionId, UserAction.ENLIST);

        // Then
        assertAll(
                () -> verifyCommonInteractions(),
                // call enlist method on student and pass the section
                () -> verify(student).enlist(section),
                // return to the same page but implement post-redirect-get pattern
                () -> assertEquals("redirect:enlist", returnPath)
        );
    }

    @Test
    void enlistOrCancel_cancel_student_enlistment() {
        // When we call the enlistOrCancel method with the CANCEL action
        String returnPath = enlistController.enlistOrCancel(student, sectionId, UserAction.CANCEL);

        // Then
        assertAll(
                () -> verifyCommonInteractions(),
                // call cancel method on student and pass the section
                () -> verify(student).cancel(section),
                // return to the same page but implement post-redirect-get pattern
                () -> assertEquals("redirect:enlist", returnPath)
        );
    }

    /**
     * Verifies the common interactions that occur in both enlist and cancel actions.
     */
    private void verifyCommonInteractions() {
        // fetch the section from the repository using the sectionId
        verify(sectionRepository).findById(sectionId);
        // fetch Hibernate session
        verify(entityManager).unwrap(Session.class);
        // reattach student to Hibernate session
        verify(session).update(student);
        // save the section
        verify(sectionRepository).save(section);
        // save the student
        verify(studentRepository).save(student);
    }
}
