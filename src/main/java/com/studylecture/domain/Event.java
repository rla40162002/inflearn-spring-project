package com.studylecture.domain;

import com.studylecture.account.UserAccount;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
    @OrderBy("enrolledAt")
    private List<Enrollment> enrollments = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private EventType eventType; // 방식. 선착순(FCFS), 확인(CONFIRMATIVE)

    public boolean isEnrollable(UserAccount userAccount) {
        return isNotClosed() && !this.isAttended(userAccount) && !isAlreadyEnrolled(userAccount);
    } // isEnrollable

    public boolean isDisenrollable(UserAccount userAccount) {
        return isNotClosed() && !this.isAttended(userAccount) && isAlreadyEnrolled(userAccount);
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

    public long numberOfAcceptedEnrollments() { // 참여한 사람
        return this.enrollments.stream().filter(Enrollment::isAccepted).count();
    } // numberOfAcceptedEnrollments

    public void addEnrollment(Enrollment enrollment) {
        this.enrollments.add(enrollment);
        enrollment.setEvent(this); // 이것도 해줘야 한다.
    } // addEnrollment

    public void removeEnrollment(Enrollment enrollment) {
        this.enrollments.remove(enrollment);
        enrollment.setEvent(null);
    } // removeEnrollment

    public boolean isAbleToAcceptWaitingEnrollment() {
        return this.eventType == EventType.FCFS && this.limitOfEnrollments > this.numberOfAcceptedEnrollments();
    } // 선착순이고 모집인원이 참가신청한 인원보다 많을 때

    public boolean canAccept(Enrollment enrollment) {
        return this.eventType == EventType.CONFIRMATIVE && this.enrollments.contains(enrollment)
                && this.limitOfEnrollments > this.numberOfAcceptedEnrollments()
                && !enrollment.isAttended() && !enrollment.isAccepted();
    } // canAccept

    public boolean canReject(Enrollment enrollment) {
        return this.eventType == EventType.CONFIRMATIVE && this.enrollments.contains(enrollment)
                && !enrollment.isAttended() && enrollment.isAccepted();
    } // canReject 참가확정 했다가 다시 취소할 수 있는지

    private List<Enrollment> getWaitingList() {
        return this.enrollments.stream().filter(enrollment -> !enrollment.isAccepted()).collect(Collectors.toList());
        // 참가 확정 짓지 않은 수
    } // getWaitingList

    public void acceptWaitingList() {
        if (this.isAbleToAcceptWaitingEnrollment()) { // 최대 인원 수정했을 때 자동으로 대기중인 인원 참가 확정 시켜주는 부분
            List<Enrollment> waitingList = getWaitingList();
            int numberToAccept = (int) Math.min(this.limitOfEnrollments - this.numberOfAcceptedEnrollments(), waitingList.size());
            waitingList.subList(0, numberToAccept).forEach(e -> e.setAccepted(true));
        }
    } // acceptWaitingList

    public void acceptNextWaitingEnrollment() {
        if (this.isAbleToAcceptWaitingEnrollment()) {
            Enrollment enrollmentToAccept = this.getTheFirstWaitingEnrollment();
            if (enrollmentToAccept != null) {
                enrollmentToAccept.setAccepted(true);
            }
        }
    } // acceptNextWaitingEnrollment

    private Enrollment getTheFirstWaitingEnrollment() {
        for (Enrollment e : this.enrollments) {
            if (!e.isAccepted()) { // 참가확정이 되지 않은 것들 중에 가장 앞에 있는 enrollment
                return e;
            }
        } // for
        return null;
    } // getTheFirstWaitingEnrollment

    public void accept(Enrollment enrollment) {
        if (this.eventType == EventType.CONFIRMATIVE && this.limitOfEnrollments > this.numberOfAcceptedEnrollments()) {
            // 관리자 확인이고, 남은 자리가 있으면 신청 수락
            enrollment.setAccepted(true);
        }
    } // accept

    public void reject(Enrollment enrollment) {
        if (this.eventType == EventType.CONFIRMATIVE) {
            // 신청 취소
            enrollment.setAccepted(false);
        }
    } // reject

}
