package com.studylecture.modules.event.event;

import com.studylecture.modules.event.Enrollment;

public class EnrollmentRejectedEvent extends EnrollmentEvent {

    public EnrollmentRejectedEvent(Enrollment enrollment) {
        super(enrollment, "모임 참가 신청을 취소했습니다.");
    }

}
