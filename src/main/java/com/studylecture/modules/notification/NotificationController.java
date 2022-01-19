package com.studylecture.modules.notification;

import com.studylecture.modules.account.Account;
import com.studylecture.modules.account.CurrentAccount;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationRepository notificationRepository;

    private final NotificationService notificationService;

    @GetMapping("/notifications")
    public String getNotifications(@CurrentAccount Account account, Model model) {
        List<Notification> notifications = notificationRepository.findByAccountAndCheckedOrderByCreatedDateTimeDesc(account, false);
        long numberOfChecked = notificationRepository.countByAccountAndChecked(account, true);

        putCategorizedNotifications(model, notifications, numberOfChecked, notifications.size());


        model.addAttribute("isNew", true);
        
        notificationService.markAsRead(notifications); // 클릭을 해서 조회가 되면 확인이 된 거니까 다 checked true 로 변경

        return "notification/list";
    } // getNotifications

    @GetMapping("/notifications/old")
    public String getOldNotifications(@CurrentAccount Account account, Model model) {
        List<Notification> notifications = notificationRepository.findByAccountAndCheckedOrderByCreatedDateTimeDesc(account, true);
        long numberOfNotChecked = notificationRepository.countByAccountAndChecked(account, false);

        putCategorizedNotifications(model, notifications, notifications.size(), numberOfNotChecked);

        model.addAttribute("isNew", false);

        return "notification/list";
    } // getOldNotifications

    @DeleteMapping("/notifications")
    public String deleteNotifications(@CurrentAccount Account account) {
        notificationRepository.deleteByAccountAndChecked(account, true);

        return "redirect:/notifications";
    }






    private void putCategorizedNotifications(Model model, List<Notification> notifications,
                                             long numberOfChecked, long numberOfNotChecked) {
        ArrayList<Notification> newStudyNotifications = new ArrayList<>();
        ArrayList<Notification> eventEnrollmentNotifications = new ArrayList<>();
        ArrayList<Notification> watchingStudyNotifications = new ArrayList<>();

        for (var notification : notifications) {
            switch (notification.getNotificationType()) {
                case STUDY_CREATED:
                    newStudyNotifications.add(notification);
                    break;
                case EVENT_ENROLLMENT:
                    eventEnrollmentNotifications.add(notification);
                    break;
                case STUDY_UPDATED:
                    watchingStudyNotifications.add(notification);
                    break;
            } // switch
        } // for

        model.addAttribute("numberOfNotChecked", numberOfNotChecked);
        model.addAttribute("numberOfChecked", numberOfChecked);
        model.addAttribute("notifications", notifications);
        model.addAttribute("newStudyNotifications", newStudyNotifications);
        model.addAttribute("eventEnrollmentNotifications", eventEnrollmentNotifications);
        model.addAttribute("watchingStudyNotifications", watchingStudyNotifications);
    } // putCategorizedNotifications

}
