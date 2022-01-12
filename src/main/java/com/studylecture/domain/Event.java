package com.studylecture.domain;

import com.studylecture.account.UserAccount;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@NamedEntityGraph(
        name = "Event.withEnrollments",
        attributeNodes = @NamedAttributeNode("enrollments")
)
@Entity
@Getter
@Setter
@EqualsAndHashCode(of = "id")
public class Event {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    private Study study; // 어느 스터디에 속한 이벤트인지

    @ManyToOne
    private Account createdBy; // 모임 주최자(만든사람)

    @Column(nullable = false)
    private String title;

    @Lob
    private String description;

    @Column(nullable = false)
    private LocalDateTime createDateTime; // 만든 일자

    @Column(nullable = false)
    private LocalDateTime endEnrollmentDateTime; // 접수 종료 일자

    @Column(nullable = false)
    private LocalDateTime startDateTime; // 모임 시작 일자

    @Column(nullable = false)
    private LocalDateTime endDateTime; // 모임 종료 일자

    @Column
    private Integer limitOfEnrollments; // 참가 신청을 최대 몇개까지 받을 수 있는지

    @OneToMany(mappedBy = "event")
    private List<Enrollment> enrollments;

    @Enumerated(EnumType.STRING)
    private EventType eventType; // 방식. 선착순(FCFS), 확인(CONFIRMATIVE)

    public boolean isEnrollable(UserAccount userAccount) {
        return isNotClosed() && !isAlreadyEnrolled(userAccount);
    } // isEnrollable

    public boolean isDisenrollable(UserAccount userAccount) {
        return isNotClosed() && isAlreadyEnrolled(userAccount);
    } // isDisenrollable


    private boolean isAlreadyEnrolled(UserAccount userAccount) {
        Account account = userAccount.getAccount();
        for (Enrollment e : this.enrollments) {
            if (e.getAccount().equals(account)) {
                return true;
            }
        } // for
        return false;
    } // isAlreadyEnrolled

    private boolean isNotClosed() {
        return this.endEnrollmentDateTime.isAfter(LocalDateTime.now());
    } // isNotClosed

    public boolean isAttended(UserAccount userAccount) {
        Account account = userAccount.getAccount();
        for (Enrollment e : enrollments) {
            if (e.getAccount().equals(account) && e.isAttended()) {
                return true;
            }
        }// for
        return false;
    } // isAttended

    public int numberOfRemainSpots() { // 남은 자리
        return this.limitOfEnrollments - (int) this.enrollments.stream().filter(Enrollment::isAccepted).count();
    } // numberOfRemainSpots

}
