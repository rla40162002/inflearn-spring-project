package com.studylecture.settings;

import com.studylecture.domain.Account;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class Notifications {

    private boolean studyCreatedByEmail;
    private boolean studyCreatedByWeb;
    private boolean studyJoinResultByEmail;
    private boolean studyJoinResultByWeb;
    private boolean studyUpdatedByEmail;
    private boolean studyUpdatedByWeb;

}
