package com.adriannebulao.enlistment.controllers;

import com.adriannebulao.enlistment.domain.*;
import org.hibernate.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.orm.*;
import org.springframework.retry.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.ui.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.*;
import org.springframework.web.servlet.view.*;

import jakarta.persistence.*;
import jakarta.transaction.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import static org.apache.commons.lang3.Validate.notNull;


@Transactional
@Controller
@RequestMapping("enlist")
@SessionAttributes("student")
class EnlistController {

    @Autowired
    private SectionRepository sectionRepo;
    @Autowired
    private StudentRepository studentRepo;
    @PersistenceContext
    private EntityManager entityManager;

    @ModelAttribute
    public void initStudent(Model model, Integer studentNumber) {
        Student student = (Student) model.getAttribute("student");
        if (studentNumber == null && student == null) {
            throw new LoginException("both studentNumber & student are null");
        }
        if (studentNumber != null && (studentNumber < 1 || studentNumber > 3)) {
            throw new LoginException("studentNumber out of range, was: " + studentNumber);
        }
        if (studentNumber != null) {
            student = studentRepo.findById(studentNumber).orElseThrow(() -> new NoSuchElementException("No student for studentNumber " + studentNumber));
            model.addAttribute(student);
        }
        model.addAttribute("isRetry", false);

    }

    @ExceptionHandler(LoginException.class)
    public RedirectView home() {
        return new RedirectView("login.html");
    }


    @GetMapping
    public String showSections(Model model, @ModelAttribute Student student) {
        var enlistedSections = student.getSections();
        model.addAttribute("enlistedSections", enlistedSections);
        model.addAttribute("availableSections", sectionRepo.findAll().stream()
                .filter(sec -> !enlistedSections.contains(sec)).collect(Collectors.toList()));
        return "enlist";
    }

    @Retryable(retryFor = ObjectOptimisticLockingFailureException.class, maxAttempts = 10)
    @PostMapping
    public String enlistOrCancel(@ModelAttribute Student student, @RequestParam String sectionId,
                                 @RequestParam UserAction userAction) {
        Section section = sectionRepo.findById(sectionId).orElseThrow(() -> new RuntimeException("Section not found"));

        updateAndAct(student, section, userAction);

        sectionRepo.save(section);
        studentRepo.save(student);

        return "redirect:enlist";
    }

    /**
     * Updates the student entity and performs the specified user action.
     *
     * @param student   the student to update
     * @param section   the section to enlist or cancel
     * @param userAction the action to perform (ENLIST or CANCEL)
     */
    private void updateAndAct(Student student, Section section, UserAction userAction) {
        Session session = entityManager.unwrap(Session.class);
        session.update(student);
        if (userAction == UserAction.ENLIST) {
            session.refresh(student); // Only refresh if enlisting
        }
        userAction.act(student, section);
    }

    @ExceptionHandler(EnlistmentException.class)
    public String handleException(RedirectAttributes redirectAttrs, EnlistmentException e) {
        redirectAttrs.addFlashAttribute("enlistmentExceptionMessage", e.getMessage());
        return "redirect:enlist";
    }

    void setSectionRepo(SectionRepository sectionRepo) {
        this.sectionRepo = sectionRepo;
    }

    void setStudentRepo(StudentRepository studentRepo) {
        this.studentRepo = studentRepo;
    }

    void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

}

enum UserAction {
    ENLIST(Student::enlist),
    CANCEL(Student::cancel);

    private final BiConsumer<Student, Section> action;

    UserAction(BiConsumer<Student, Section> action) {
        this.action = action;
    }

    void act(Student student, Section section) {
        action.accept(student, section);
    }

}
