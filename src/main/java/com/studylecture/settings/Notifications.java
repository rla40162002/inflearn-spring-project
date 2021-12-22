package com.studylecture.settings;

import com.studylecture.domain.Account;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Notifications {


    private boolean studyCreatedByEmail;
    private boolean studyCreatedByWeb;
    private boolean studyJoinResultByEmail;
    private boolean studyJoinResultByWeb;
    private boolean studyUpdatedByEmail;
    private boolean studyUpdatedByWeb;

    public Notifications(Account account) {
        this.studyCreatedByEmail = account.isStudyCreatedByEmail();
        this.studyCreatedByWeb = account.isStudyCreatedByWeb();
        this.studyJoinResultByEmail = account.isStudyJoinResultByEmail();
        this.studyJoinResultByWeb = account.isStudyJoinResultByWeb();
        this.studyUpdatedByEmail = account.isStudyUpdatedByEmail();
        this.studyUpdatedByWeb = account.isStudyUpdatedByWeb();
    }
}
