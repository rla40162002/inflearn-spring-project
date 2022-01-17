package com.studylecture.modules.event;

import com.studylecture.infra.MockMvcTest;
import com.studylecture.modules.account.Account;
import com.studylecture.modules.account.AccountFactory;
import com.studylecture.modules.account.AccountRepository;
import com.studylecture.modules.account.WithAccount;
import com.studylecture.modules.study.Study;
import com.studylecture.modules.study.StudyFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@MockMvcTest
class EventControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    EventService eventService;
    @Autowired
    EnrollmentRepository enrollmentRepository;
    @Autowired
    AccountFactory accountFactory;
    @Autowired
    StudyFactory studyFactory;
    @Autowired
    AccountRepository accountRepository;

    @Test
    @DisplayName("선착순 모임에 참가 신청 - 자동 수락")
    @WithAccount("kyw")
    void newEnrollmentToFCFSEventAccepted() throws Exception {
        Account kyw1023 = accountFactory.createAccount("kyw1023");
        Study study = studyFactory.createStudy("study-test", kyw1023);
        Event event = createEvent("test-event", EventType.FCFS, 2, study, kyw1023);

        mockMvc.perform(post("/study/" + study.getPath() + "/events/" + event.getId() + "/enroll")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/" + study.getPath() + "/events/" + event.getId()));
        Account kyw = accountRepository.findByNickname("kyw");
        isAccepted(kyw, event);

    } // newEnrollmentToFCFSEventAccepted

    @Test
    @DisplayName("선착순 모임에 참가 신청 - 대기중(이미 꽉찬 상태)")
    @WithAccount("kyw")
    void newEnrollmentToFCFSEventNotAccepted() throws Exception {
        Account kyw1023 = accountFactory.createAccount("kyw1023");
        Study study = studyFactory.createStudy("study-test", kyw1023);
        Event event = createEvent("test-event", EventType.FCFS, 2, study, kyw1023);

        Account testAccount = accountFactory.createAccount("testAccount");
        Account testAccount2 = accountFactory.createAccount("testAccount2");
        eventService.newEnrollment(event, testAccount);
        eventService.newEnrollment(event, testAccount2);

        mockMvc.perform(post("/study/" + study.getPath() + "/events/" + event.getId() + "/enroll")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/" + study.getPath() + "/events/" + event.getId()));
        Account kyw = accountRepository.findByNickname("kyw");
        isNotAccepted(kyw, event);

    } // newEnrollmentToFCFSEventNotAccepted

    @Test
    @DisplayName("참가신청 확정자가 선착순 모임에 참가 신청을 취소하는 경우, 바로 다음 대기자가 자동으로 신청 확인")
    @WithAccount("kyw")
    void acceptedAccountCancelEnrollmentToFCFSEventNotAccepted() throws Exception {
        Account kyw = accountRepository.findByNickname("kyw");
        Account kyw1023 = accountFactory.createAccount("kyw1023");
        Study study = studyFactory.createStudy("study-test", kyw1023);
        Event event = createEvent("test-event", EventType.FCFS, 2, study, kyw1023);

        Account testAccount = accountFactory.createAccount("testAccount");
        eventService.newEnrollment(event, testAccount);
        eventService.newEnrollment(event, kyw);
        eventService.newEnrollment(event, kyw1023);

        isAccepted(testAccount, event);
        isAccepted(kyw, event);
        isNotAccepted(kyw1023, event);

        mockMvc.perform(post("/study/" + study.getPath() + "/events/" + event.getId() + "/disenroll")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/" + study.getPath() + "/events/" + event.getId()));

        isAccepted(testAccount, event);
        isAccepted(kyw1023, event);
        assertNull(enrollmentRepository.findByEventAndAccount(event, kyw));

    } // acceptedAccountCancelEnrollmentToFCFSEventNotAccepted

    @Test
    @DisplayName("참가신청 비확정자가 선착순 모임에 참가 신청을 취소하는 경우, 기존 확정자를 그대로 유지하고 새로운 확정자는 없다.")
    @WithAccount("kyw")
    void notAcceptedAccountCancelEnrollmentToFCFSEventNotAccepted() throws Exception {
        Account kyw = accountRepository.findByNickname("kyw");
        Account kyw1023 = accountFactory.createAccount("kyw1023");
        Study study = studyFactory.createStudy("study-test", kyw1023);
        Event event = createEvent("test-event", EventType.FCFS, 2, study, kyw1023);

        Account testAccount = accountFactory.createAccount("testAccount");
        eventService.newEnrollment(event, testAccount);
        eventService.newEnrollment(event, kyw1023);
        eventService.newEnrollment(event, kyw);

        isAccepted(testAccount, event);
        isAccepted(kyw1023, event);
        isNotAccepted(kyw, event);

        mockMvc.perform(post("/study/" + study.getPath() + "/events/" + event.getId() + "/disenroll")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/" + study.getPath() + "/events/" + event.getId()));

        isAccepted(testAccount, event);
        isAccepted(kyw1023, event);
        assertNull(enrollmentRepository.findByEventAndAccount(event, kyw));

    } // notAcceptedAccountCancelEnrollmentToFCFSEventNotAccepted

    @Test
    @DisplayName("관리자 확인 모임에 참가 신청 - 대기중")
    @WithAccount("kyw")
    void newEnrollmentToCONFIRMATIVEEventNotAccepted() throws Exception {
        Account kyw1023 = accountFactory.createAccount("kyw1023");
        Study study = studyFactory.createStudy("study-test", kyw1023);
        Event event = createEvent("test-event", EventType.CONFIRMATIVE, 2, study, kyw1023);

        mockMvc.perform(post("/study/" + study.getPath() + "/events/" + event.getId() + "/enroll")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/" + study.getPath() + "/events/" + event.getId()));


        Account kyw = accountRepository.findByNickname("kyw");

        isNotAccepted(kyw, event);

    } // newEnrollmentToCONFIRMATIVEEventNotAccepted


    private void isAccepted(Account account, Event event) {
        assertTrue(enrollmentRepository.findByEventAndAccount(event, account).isAccepted());
    }

    private void isNotAccepted(Account account, Event event) {
        assertFalse(enrollmentRepository.findByEventAndAccount(event, account).isAccepted());
    }

    private Event createEvent(String eventTitle, EventType eventType, int limit, Study study, Account account) {
        Event event = new Event();
        event.setEventType(eventType);
        event.setLimitOfEnrollments(limit);
        event.setTitle(eventTitle);
        event.setCreateDateTime(LocalDateTime.now());
        event.setEndEnrollmentDateTime(LocalDateTime.now().plusDays(1));
        event.setStartDateTime(LocalDateTime.now().plusDays(1).plusHours(5));
        event.setEndDateTime(LocalDateTime.now().plusDays(1).plusHours(7));
        return eventService.createEvent(event, study, account);
    }
}