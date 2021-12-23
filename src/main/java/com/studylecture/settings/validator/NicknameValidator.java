package com.studylecture.settings.validator;

import com.studylecture.account.AccountRepository;
import com.studylecture.domain.Account;
import com.studylecture.settings.form.NicknameForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class NicknameValidator implements Validator {

    private final AccountRepository accountRepository;

    @Override
    public boolean supports(Class<?> clazz) {
        return NicknameForm.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        NicknameForm nicknameForm = (NicknameForm) target;
        Account byNickname = accountRepository.findByNickname(nicknameForm.getNickname());

        if (byNickname != null) {
            errors.rejectValue("nickname", "wrong.value", "입력하신 닉네임을 사용할 수 없습니다.(이미 존재하는 닉네임)");
        }
    }
}
