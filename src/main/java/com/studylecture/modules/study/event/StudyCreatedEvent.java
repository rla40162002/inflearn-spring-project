package com.studylecture.modules.study.event;

import com.studylecture.modules.study.Study;
import lombok.Getter;

@Getter
public class StudyCreatedEvent{

    private Study study;
    public StudyCreatedEvent(Study study) {
        this.study = study;
    }
}
