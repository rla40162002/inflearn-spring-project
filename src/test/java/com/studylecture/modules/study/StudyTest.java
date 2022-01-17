package com.studylecture.modules.study;

import com.studylecture.modules.account.Account;
import com.studylecture.modules.account.UserAccount;
import com.studylecture.modules.study.Study;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StudyTest {
    Study study;
    Account account;
    UserAccount userAccount;

    @BeforeEach
    void beforeEach() {
        study = new Study();
        account = new Account();
        account.setNickname("kyw");
        account.setPassword("123");
        userAccount = new UserAccount(account);
    }

    @DisplayName("스터디 공개(true), 인원 모집 중(true), 이미 해당 스터디 그룹에 가입되어 있는 경우가 아니라면 가입 가능")
    @Test
    void isJoinable() {
        study.setPublished(true);
        study.setRecruiting(true);

        assertTrue(study.isJoinable(userAccount));
    } // 가입 가능 여부(스터디 공개, 인원 모집 중일 때 가입되어 있지 않은 회원이 온 경우)

    @DisplayName("스터디 공개, 인원 모집 중, 스터디 관리자는 가입이 불필요")
    @Test
    void isJoinableFalseForManager() {
        study.setPublished(true);
        study.setRecruiting(true);
        study.addManager(account);

        assertFalse(study.isJoinable(userAccount));
    }

    @DisplayName("스터디 공개, 인원 모집 중, 스터디 멤버는 가입이 불필요")
    @Test
    void isJoinableFalseForMember() {
        study.setPublished(true);
        study.setRecruiting(true);
        study.addMember(account);

        assertFalse(study.isJoinable(userAccount));
    }

    @DisplayName("스터디가 비공개이거나 인원 모집 중이 아니라면 스터디 가입이 불가능하다.")
    @Test
    void isJoinableFalseNotRecruitingOrNotPublished() {
        study.setPublished(true);
        study.setRecruiting(false);

        assertFalse(study.isJoinable(userAccount));

        study.setPublished(false);
        study.setRecruiting(true);

        assertFalse(study.isJoinable(userAccount));
    }

    @DisplayName("스터디 관리자인지 확인")
    @Test
    void isManager() {
        study.addManager(account);
        assertTrue(study.isManager(userAccount));
    }

    @DisplayName("스터디 멤버인지 확인")
    @Test
    void isMember() {
        study.addMember(account);
        assertTrue(study.isMember(userAccount));
    }
}