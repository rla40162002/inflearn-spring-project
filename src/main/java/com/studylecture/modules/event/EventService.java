package com.studylecture.modules.event;

import com.studylecture.modules.account.Account;
import com.studylecture.modules.event.event.EnrollmentAcceptedEvent;
import com.studylecture.modules.event.event.EnrollmentRejectedEvent;
import com.studylecture.modules.study.Study;
import com.studylecture.modules.event.form.EventForm;
import com.studylecture.modules.study.event.StudyUpdateEvent;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
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
    private final ApplicationEventPublisher eventPublisher;

    public Event createEvent(Event event, Study study, Account account) {
        event.setCreatedBy(account);
        event.setCreateDateTime(LocalDateTime.now());
        event.setStudy(study);
        eventPublisher.publishEvent(new StudyUpdateEvent(event.getStudy(), "'" + event.getTitle() + "' 모임을 만들었습니다."));
        return eventRepository.save(event);
    } // createEvent

    public void updateEvent(Event event, EventForm eventForm) {
        modelMapper.map(eventForm, event);
        event.acceptWaitingList();
        eventPublisher.publishEvent(new StudyUpdateEvent(event.getStudy(), "'" + event.getTitle() + "' 모임이 수정되었습니다."));
    } // updateEvent

    public void deleteEvent(Event event) {
        eventRepository.delete(event);
        // TODO : Enrollment가 있을 때 삭제가 되지 않는 문제
        eventPublisher.publishEvent(new StudyUpdateEvent(event.getStudy(), "'" + event.getTitle() + "' 모임이 삭제되었습니다."));
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
        eventPublisher.publishEvent(new EnrollmentAcceptedEvent(enrollment));
    } // acceptEnrollment

    public void rejectEnrollment(Event event, Enrollment enrollment) {
        event.reject(enrollment);
        eventPublisher.publishEvent(new EnrollmentRejectedEvent(enrollment));
    } // rejectEnrollment

    public void checkInEnrollment(Enrollment enrollment) {
        enrollment.setAttended(true);
    } // checkInEnrollment

    public void cancelCheckInEnrollment(Enrollment enrollment) {
        enrollment.setAttended(false);
    } // cancelCheckInEnrollment


}
