package com.adriannebulao.enlistment.controllers;

import com.adriannebulao.enlistment.domain.*;
import com.adriannebulao.enlistment.domain.Period;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;
import org.springframework.ui.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.*;

import java.time.*;
import java.util.*;

@Transactional
@Controller
@RequestMapping("sections")
@SessionAttributes("admin")
class SectionsController {

    @Autowired
    private SubjectRepository subjectRepo;
    @Autowired
    private AdminRepository adminRepo;
    @Autowired
    private RoomRepository roomRepo;
    @Autowired
    private SectionRepository sectionRepo;

    @ModelAttribute("admin")
    public Admin admin(Integer id) {
        return adminRepo.findById(id).orElseThrow(() -> new NoSuchElementException("no admin found for adminId " + id));
    }

    @GetMapping
    public String showPage(Model model, Integer id) {
        Admin admin = id == null ? (Admin) model.getAttribute("admin") :
                adminRepo.findById(id).orElseThrow(() -> new NoSuchElementException("no admin found for adminId " + id));
        model.addAttribute("admin", admin);
        model.addAttribute("subjects", subjectRepo.findAll());
        model.addAttribute("rooms", roomRepo.findAll());
        model.addAttribute("sections", sectionRepo.findAll());
        return "sections";
    }

    @PostMapping
    public String createSection(@RequestParam String sectionId, @RequestParam String subjectId, @RequestParam Days days,
                                @RequestParam String start, @RequestParam String end, @RequestParam String roomName,
                                RedirectAttributes redirectAttrs) {
        try {
            // Retrieve and validate Subject, Schedule, and Room
            Subject subject = getSubjectById(subjectId);
            Schedule schedule = createSchedule(days, start, end);
            Room room = getRoomByName(roomName);

            // Create and save new Section
            sectionRepo.save(new Section(sectionId, subject, schedule, room));

            // Add success message to redirect attributes
            redirectAttrs.addFlashAttribute("sectionSuccessMessage", "Successfully created new section " + sectionId);
        } catch (NoSuchElementException | IllegalArgumentException e) {
            // Add error message to redirect attributes
            redirectAttrs.addFlashAttribute("sectionExceptionMessage", e.getMessage());
        }
        return "redirect:sections";
    }

    /**
     * Retrieves a Subject by its ID.
     *
     * @param subjectId the ID of the subject
     * @return the Subject object
     * @throws NoSuchElementException if no subject is found with the given ID
     */
    private Subject getSubjectById(String subjectId) {
        return subjectRepo.findById(subjectId)
                .orElseThrow(() -> new NoSuchElementException("No subject found with subject ID: " + subjectId));
    }

    /**
     * Creates a Schedule object from the given parameters.
     *
     * @param days the days of the schedule
     * @param start the start time as a string
     * @param end the end time as a string
     * @return the Schedule object
     */
    private Schedule createSchedule(Days days, String start, String end) {
        Period period = new Period(LocalTime.parse(start), LocalTime.parse(end));
        return new Schedule(days, period);
    }

    /**
     * Retrieves a Room by its name.
     *
     * @param roomName the name of the room
     * @return the Room object
     * @throws NoSuchElementException if no room is found with the given name
     */
    private Room getRoomByName(String roomName) {
        return roomRepo.findById(roomName)
                .orElseThrow(() -> new NoSuchElementException("No room found with room name: " + roomName));
    }

    @ExceptionHandler(EnlistmentException.class)
    public String handleException(RedirectAttributes redirectAttrs, EnlistmentException e) {
        redirectAttrs.addFlashAttribute("sectionExceptionMessage", e.getMessage());
        return "redirect:sections";
    }

    void setSubjectRepo(SubjectRepository subjectRepo) {
        this.subjectRepo = subjectRepo;
    }

    void setSectionRepo(SectionRepository sectionRepo) {
        this.sectionRepo = sectionRepo;
    }

    void setRoomRepo(RoomRepository roomRepo) {
        this.roomRepo = roomRepo;
    }

    void setAdminRepo(AdminRepository adminRepo) {
        this.adminRepo = adminRepo;
    }



}
