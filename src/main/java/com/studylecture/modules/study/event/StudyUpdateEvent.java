package com.studylecture.modules.study.event;

import com.studylecture.modules.study.Study;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class StudyUpdateEvent {
    private final Study study;
    private final String message;
}
