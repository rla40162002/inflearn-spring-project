package com.studylecture.modules.event.event;

import com.studylecture.infra.config.AppProperties;
import com.studylecture.infra.mail.EmailMessage;
import com.studylecture.infra.mail.EmailService;
import com.studylecture.modules.account.Account;
import com.studylecture.modules.account.AccountPredicates;
import com.studylecture.modules.event.Enrollment;
import com.studylecture.modules.event.Event;
import com.studylecture.modules.notification.Notification;
import com.studylecture.modules.notification.NotificationRepository;
import com.studylecture.modules.notification.NotificationType;
import com.studylecture.modules.study.Study;
import com.studylecture.modules.study.event.StudyCreatedEvent;
import com.studylecture.modules.study.event.StudyUpdateEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Async
@Component
@Transactional
@RequiredArgsConstructor
public class EnrollmentEventListener {

    private final NotificationRepository notificationRepository;
    private final AppProperties appProperties;
    private final TemplateEngine templateEngine;
    private final EmailService emailService;

    @EventListener
    public void handleEnrollmentEvent(EnrollmentEvent enrollmentEvent) {
        Enrollment enrollment = enrollmentEvent.getEnrollment();
        Account account = enrollment.getAccount();
        Event event = enrollment.getEvent();
        Study study = event.getStudy();

        if (account.isStudyJoinResultByEmail()) {
            sendMail(enrollmentEvent, account, event, study);
        }
        if (account.isStudyJoinResultByWeb()) {
            createNotification(enrollmentEvent, account, event, study);
        }
    } // handleEnrollmentEvent


    private void sendMail(EnrollmentEvent enrollmentEvent, Account account, Event event, Study study) {
        Context context = new Context();
        context.setVariable("nickname", account.getNickname());
        context.setVariable("link", "/study/" + study.getEncodePath() + "/events/" + event.getId());
        context.setVariable("linkName", study.getTitle());
        context.setVariable("message", enrollmentEvent.getMessage());
        context.setVariable("host", appProperties.getHost());
        String message = templateEngine.process("mail/simple-link", context);

        EmailMessage emailMessage = EmailMessage.builder()
                .to(account.getEmail())
                .subject("스터디 사이트 " + event.getTitle() + " 모임 참가 신청 결과입니다.")
                .message(message).build();

        emailService.sendEmail(emailMessage);
    } // sendStudyCreatedEmail

    private void createNotification(EnrollmentEvent enrollmentEvent, Account account, Event event, Study study) {
        Notification notification = new Notification();

        notification.setTitle(study.getTitle() + " / " + event.getTitle());
        notification.setLink("/study/" + study.getEncodePath() + "/events/" + event.getId());
        notification.setChecked(false);
        notification.setCreatedDateTime(LocalDateTime.now());
        notification.setMessage(enrollmentEvent.getMessage());
        notification.setAccount(account);
        notification.setNotificationType(NotificationType.EVENT_ENROLLMENT);

        notificationRepository.save(notification);
    } // saveStudyCreatedNotification


}
