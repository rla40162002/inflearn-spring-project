package com.studylecture.modules.event;

import com.studylecture.modules.account.CurrentAccount;
import com.studylecture.modules.account.Account;
import com.studylecture.modules.study.Study;
import com.studylecture.modules.event.form.EventForm;
import com.studylecture.modules.event.validator.EventValidator;
import com.studylecture.modules.study.StudyRepository;
import com.studylecture.modules.study.StudyService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/study/{path}")
@RequiredArgsConstructor
public class EventController {

    private final StudyRepository studyRepository;
    private final StudyService studyService;
    private final EventService eventService;
    private final ModelMapper modelMapper;
    private final EventValidator eventValidator;
    private final EventRepository eventRepository;
    private final EnrollmentRepository enrollmentRepository;

    @InitBinder("eventForm")
    public void initBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(eventValidator);
    } // initBinder

    @GetMapping("/new-event")
    public String newEventForm(@CurrentAccount Account account, @PathVariable String path, Model model) {
        Study study = studyService.getStudyToUpdateStatus(account, path);
        model.addAttribute(account);
        model.addAttribute(study);
        model.addAttribute(new EventForm());
        return "event/form";
    } // newEventForm

    @PostMapping("/new-event")
    public String submitNewEvent(@CurrentAccount Account account, @PathVariable String path,
                                 @Valid EventForm eventForm, Errors errors, Model model) {
        Study study = studyService.getStudyToUpdateStatus(account, path);

        if (errors.hasErrors()) {
            model.addAttribute(account);
            model.addAttribute(study);
            return "event/form";
        }

        Event event = eventService.createEvent(modelMapper.map(eventForm, Event.class), study, account);
        return "redirect:/study/" + study.getEncodePath() + "/events/" + event.getId();
    } // submitNewEvent

    @GetMapping("/events/{id}")
    public String getEvent(@CurrentAccount Account account, @PathVariable String path, @PathVariable("id") Event event,
                           Model model) {

        model.addAttribute(account);
        model.addAttribute(event);
        model.addAttribute(studyRepository.findStudyWithManagersByPath(path));

        return "event/view";
    } // getEvent

    @GetMapping("/events")
    public String viewEvents(@CurrentAccount Account account, @PathVariable String path, Model model) {
        Study study = studyService.getStudy(path);
        List<Event> events = eventRepository.findByStudyOrderByStartDateTime(study);
        List<Event> newEvents = new ArrayList<>();
        List<Event> oldEvents = new ArrayList<>();

        events.forEach(e -> {
            if (e.getEndDateTime().isBefore(LocalDateTime.now())) {
                oldEvents.add(e);
            } else {
                newEvents.add(e);
            }
        });

        model.addAttribute(account);
        model.addAttribute(study);
        model.addAttribute("newEvents", newEvents);
        model.addAttribute("oldEvents", oldEvents);

        return "study/events";

    } // viewEvents

    @GetMapping("/events/{id}/edit")
    public String updateEventForm(@CurrentAccount Account account, @PathVariable String path, @PathVariable("id") Event event, Model model) {
        Study study = studyService.getStudyToUpdate(account, path);

        model.addAttribute(account);
        model.addAttribute(study);
        model.addAttribute(event);
        model.addAttribute(modelMapper.map(event, EventForm.class));

        return "event/update-form";
    } // updateEventForm

    @PostMapping("/events/{id}/edit")
    public String updateEventSubmit(@CurrentAccount Account account, @PathVariable String path,
                                    @PathVariable("id") Event event,
                                    @Valid EventForm eventForm, Errors errors, Model model) {
        Study study = studyService.getStudyToUpdate(account, path);

        eventForm.setEventType(event.getEventType()); // 이상한 입력이 들어오더라도 기존 타입으로 고정(덮어쓰기)
        eventValidator.validateUpdateForm(eventForm, event, errors);

        if (errors.hasErrors()) {
            model.addAttribute(study);
            model.addAttribute(account);
            model.addAttribute(event);
            return "event/update-form";
        }

        eventService.updateEvent(event, eventForm);
        return "redirect:/study/" + study.getEncodePath() + "/events/" + event.getId();
    } // updateEventSubmit

    @DeleteMapping("/events/{id}")
    public String cancelEvent(@CurrentAccount Account account, @PathVariable String path, @PathVariable("id") Event event) {
        Study study = studyService.getStudyToUpdateStatus(account, path);
        eventService.deleteEvent(event);

        return "redirect:/study/" + study.getEncodePath() + "/events";
    } // cancelEvent

    @PostMapping("/events/{id}/enroll")
    public String newEnrollment(@CurrentAccount Account account, @PathVariable String path,
                                @PathVariable("id") Event event) {
        Study study = studyService.getStudyToEnroll(path); // 관리자가 아니어도 불러올 수 있도록
        eventService.newEnrollment(event, account);
        return "redirect:/study/" + study.getEncodePath() + "/events/" + event.getId();
    } // newEnrollment

    @PostMapping("/events/{id}/disenroll")
    public String cancelEnrollment(@CurrentAccount Account account, @PathVariable String path, @PathVariable("id") Event event) {
        Study study = studyService.getStudyToEnroll(path); // 관리자가 아니어도 불러올 수 있도록
        eventService.cancelEnrollment(event, account);
        return "redirect:/study/" + study.getEncodePath() + "/events/" + event.getId();
    } // cancelEnrollment

    @GetMapping("/events/{eventId}/enrollments/{enrollmentId}/accept")
    public String acceptEnrollment(@CurrentAccount Account account, @PathVariable String path,
                                   @PathVariable("eventId") Event event, @PathVariable("enrollmentId") Enrollment enrollment) {
        Study study = studyService.getStudyToUpdate(account, path);
        eventService.acceptEnrollment(event, enrollment);

        return "redirect:/study/" + study.getEncodePath() + "/events/" + event.getId();
    } // acceptEnrollment

    @GetMapping("/events/{eventId}/enrollments/{enrollmentId}/reject")
    public String rejectEnrollment(@CurrentAccount Account account, @PathVariable String path,
                                   @PathVariable("eventId") Event event, @PathVariable("enrollmentId") Enrollment enrollment) {
        Study study = studyService.getStudyToUpdate(account, path);
        eventService.rejectEnrollment(event, enrollment);

        return "redirect:/study/" + study.getEncodePath() + "/events/" + event.getId();
    } // rejectEnrollment

    @GetMapping("/events/{eventId}/enrollments/{enrollmentId}/checkin")
    public String checkInEnrollment(@CurrentAccount Account account, @PathVariable String path,
                                   @PathVariable("eventId") Event event, @PathVariable("enrollmentId") Enrollment enrollment) {
        Study study = studyService.getStudyToUpdate(account, path);
        eventService.checkInEnrollment(enrollment);

        return "redirect:/study/" + study.getEncodePath() + "/events/" + event.getId();
    } // checkInEnrollment

    @GetMapping("/events/{eventId}/enrollments/{enrollmentId}/cancel-checkin")
    public String cancelCheckInEnrollment(@CurrentAccount Account account, @PathVariable String path,
                                   @PathVariable("eventId") Event event, @PathVariable("enrollmentId") Enrollment enrollment) {
        Study study = studyService.getStudyToUpdate(account, path);
        eventService.cancelCheckInEnrollment(enrollment);

        return "redirect:/study/" + study.getEncodePath() + "/events/" + event.getId();
    } // cancelCheckInEnrollment

}
