package com.studylecture.modules.study.event;

import com.studylecture.infra.config.AppProperties;
import com.studylecture.infra.mail.EmailMessage;
import com.studylecture.infra.mail.EmailService;
import com.studylecture.modules.account.Account;
import com.studylecture.modules.account.AccountPredicates;
import com.studylecture.modules.account.AccountRepository;
import com.studylecture.modules.notification.Notification;
import com.studylecture.modules.notification.NotificationRepository;
import com.studylecture.modules.notification.NotificationType;
import com.studylecture.modules.study.Study;
import com.studylecture.modules.study.StudyRepository;
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
import java.util.List;
import java.util.Set;

@Slf4j
@Async
@Component
@Transactional
@RequiredArgsConstructor
public class StudyEventListener {

    private final StudyRepository studyRepository;
    private final AccountRepository accountRepository;
    private final EmailService emailService;
    private final TemplateEngine templateEngine;
    private final AppProperties appProperties;
    private final NotificationRepository notificationRepository;


    @EventListener
    public void handleStudyCreatedEvent(StudyCreatedEvent studyCreatedEvent) {
        Study study = studyRepository.findStudyWithTagsAndZonesById(studyCreatedEvent.getStudy().getId());
        Iterable<Account> accounts = accountRepository.findAll(AccountPredicates.findByTagsAndZones(study.getTags(), study.getZones()));

        accounts.forEach(account -> {
            if (account.isStudyCreatedByEmail()) {
                sendStudyCreatedEmail(study, account, "새로운 스터디가 생겼습니다.", "스터디사이트, '" + study.getTitle()
                        + "' 스터디가 생겼습니다.");
            }
            if (account.isStudyCreatedByWeb()) {
                createNotification(study, account, study.getShortDescription(), NotificationType.STUDY_CREATED);
            }
        });
    } // handleStudyCreatedEvent

    @EventListener
    public void handleStudyUpdatedEvent(StudyUpdateEvent studyUpdateEvent) {
        Study study = studyRepository.findStudyWithManagersAndMembersById(studyUpdateEvent.getStudy().getId());
        Set<Account> accounts = new HashSet<>();
        accounts.addAll(study.getManagers());
        accounts.addAll(study.getMembers());

        accounts.forEach(account -> {
            if (account.isStudyUpdatedByEmail()) {
                sendStudyCreatedEmail(study, account, studyUpdateEvent.getMessage(),
                        "스터디 사이트, '" + study.getTitle() + "' 스터디에 새소식이 있습니다.");
            }
            if (account.isStudyUpdatedByWeb()) {
                createNotification(study, account, studyUpdateEvent.getMessage(), NotificationType.STUDY_UPDATED);
            }
        });

    } // handleStudyUpdatedEvent


    private void sendStudyCreatedEmail(Study study, Account account, String contextMessage, String emailSubject) {
        Context context = new Context();
        context.setVariable("nickname", account.getNickname());
        context.setVariable("link", "/study/" + study.getEncodePath());
        context.setVariable("linkName", study.getTitle());
        context.setVariable("message", contextMessage);
        context.setVariable("host", appProperties.getHost());
        String message = templateEngine.process("mail/simple-link", context);

        EmailMessage emailMessage = EmailMessage.builder()
                .to(account.getEmail())
                .subject(emailSubject)
                .message(message).build();

        emailService.sendEmail(emailMessage);
    } // sendStudyCreatedEmail

    private void createNotification(Study study, Account account, String message, NotificationType notificationType) {
        Notification notification = new Notification();

        notification.setTitle(study.getTitle());
        notification.setLink("/study/" + study.getEncodePath());
        notification.setChecked(false);
        notification.setCreatedDateTime(LocalDateTime.now());
        notification.setMessage(message);
        notification.setAccount(account);
        notification.setNotificationType(notificationType);

        notificationRepository.save(notification);
    } // saveStudyCreatedNotification

}
