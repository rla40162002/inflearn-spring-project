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
import java.util.List;

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
                sendStudyCreatedEmail(study, account);
            }
            if (account.isStudyCreatedByWeb()) {
                saveStudyCreatedNotification(study, account);
            }
        });
    } // handleStudyCreatedEvent

    private void sendStudyCreatedEmail(Study study, Account account) {
        Context context = new Context();
        context.setVariable("nickname", account.getNickname());
        context.setVariable("link", "/study/" + study.getEncodePath());
        context.setVariable("linkName", study.getTitle());
        context.setVariable("message", "새로운 스터디가 개설되었습니다.");
        context.setVariable("host", appProperties.getHost());
        String message = templateEngine.process("mail/simple-link", context);

        EmailMessage emailMessage = EmailMessage.builder()
                .to(account.getEmail())
                .subject("스터디 사이트, '" + study.getTitle() + "' 스터디가 생겼습니다.")
                .message(message).build();

        emailService.sendEmail(emailMessage);
    } // sendStudyCreatedEmail

    private void saveStudyCreatedNotification(Study study, Account account) {
        Notification notification = new Notification();

        notification.setTitle(study.getTitle());
        notification.setLink("/study/" + study.getEncodePath());
        notification.setChecked(false);
        notification.setCreatedLocalDateTime(LocalDateTime.now());
        notification.setMessage(study.getShortDescription());
        notification.setAccount(account);
        notification.setNotificationType(NotificationType.STUDY_CREATED);

        notificationRepository.save(notification);
    } // saveStudyCreatedNotification

}
