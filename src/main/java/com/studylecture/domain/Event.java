package com.studylecture.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

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
    private Account createBy; // 모임 주최자(만든사람)

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
}
