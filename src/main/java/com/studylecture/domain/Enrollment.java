package com.studylecture.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@EqualsAndHashCode(of = "id")
public class Enrollment { // 이벤트에 대한 참여를 관리 하는 부분
    // 누가 언제 신청을 했고, 참석 했는지 확인
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    private Event event; // 어떤 이벤트에 대한 참가신청인지

    @ManyToOne
    private Account account; // 누가 신청했는지

    private LocalDateTime enrolledAt; // 언제 신청했는지, (순서가 될 수 있음)

    private boolean accepted; // 참가 확정 여부

    private boolean attended; // 실제로 참석 여부

}
