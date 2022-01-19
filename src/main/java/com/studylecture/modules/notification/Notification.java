package com.studylecture.modules.notification;

import com.studylecture.modules.account.Account;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@EqualsAndHashCode(of = "id")
public class Notification {

    @Id
    @GeneratedValue
    private Long id;

    private String title;

    private String link;

    private String message; // 짧은 메시지, Lob X

    private boolean checked; // 체크 여부

    @ManyToOne
    private Account account;

    private LocalDateTime createdDateTime;

    @Enumerated(EnumType.STRING)
    private NotificationType notificationType;
    // 스터디 개설됐을 때, 스터디 정보가 바뀔 때(새로운 모임 등), 모임 참가 신청 결과 알림
}
