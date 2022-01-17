package com.studylecture.modules.account.form;

import lombok.Data;

@Data
public class Notifications {

    private boolean studyCreatedByEmail;
    private boolean studyCreatedByWeb;
    private boolean studyJoinResultByEmail;
    private boolean studyJoinResultByWeb;
    private boolean studyUpdatedByEmail;
    private boolean studyUpdatedByWeb;

}
