package com.studylecture.modules.account;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccountFactory {

    @Autowired
    AccountRepository accountRepository;

    public Account createAccount(String nickname) {
        Account kyw = new Account();
        kyw.setNickname(nickname);
        kyw.setEmail(nickname + "@email.com");
        accountRepository.save(kyw);
        return kyw;
    } // createAccount

}
