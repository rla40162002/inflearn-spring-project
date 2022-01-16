package com.studylecture.event;

import com.studylecture.domain.Account;
import com.studylecture.domain.Enrollment;
import com.studylecture.domain.Event;
import com.studylecture.domain.Study;
import com.studylecture.event.form.EventForm;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class EventService {
    private final EventRepository eventRepository;
    private final ModelMapper modelMapper;
    private final EnrollmentRepository enrollmentRepository;

    public Event createEvent(Event event, Study study, Account account) {
        event.setCreatedBy(account);
        event.setCreateDateTime(LocalDateTime.now());
        event.setStudy(study);
        return eventRepository.save(event);
    } // createEvent

    public void updateEvent(Event event, EventForm eventForm) {
        modelMapper.map(eventForm, event);
        // TODO 모집 인원을 늘린 선착순 모임의 경우, 자동으로 추가 인원의 참가 신청을 확정 상태로 변경해야 함
        event.acceptWaitingList();
    } // updateEvent

    public void deleteEvent(Event event) {
        eventRepository.delete(event);
    } // deleteEvent

    public void newEnrollment(Event event, Account account) {
        if (!enrollmentRepository.existsByEventAndAccount(event, account)) {
            Enrollment enrollment = new Enrollment();
            enrollment.setEnrolledAt(LocalDateTime.now());
            enrollment.setAccepted(event.isAbleToAcceptWaitingEnrollment());
            enrollment.setAccount(account);
            event.addEnrollment(enrollment);
            enrollmentRepository.save(enrollment);
        }
    } // newEnrollment

    public void cancelEnrollment(Event event, Account account) {
        Enrollment enrollment = enrollmentRepository.findByEventAndAccount(event, account);
        if (!enrollment.isAttended()) { // 출석을 하지 않았을 경우에만 취소 가능
            event.removeEnrollment(enrollment);
            enrollmentRepository.delete(enrollment);
            event.acceptNextWaitingEnrollment(); // 다음 대기 건수 자동 승인
        }
    } // cancelEnrollment

    public void acceptEnrollment(Event event, Enrollment enrollment) {
        event.accept(enrollment);
    } // acceptEnrollment
    public void rejectEnrollment(Event event, Enrollment enrollment) {
        event.reject(enrollment);
    } // rejectEnrollment

    public void checkInEnrollment(Enrollment enrollment) {
        enrollment.setAttended(true);
    } // checkInEnrollment
    public void cancelCheckInEnrollment(Enrollment enrollment) {
        enrollment.setAttended(false);
    } // cancelCheckInEnrollment


}
