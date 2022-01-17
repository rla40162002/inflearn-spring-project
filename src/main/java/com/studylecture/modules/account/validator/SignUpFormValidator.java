package com.studylecture.modules.account.validator;

import com.studylecture.modules.account.AccountRepository;
import com.studylecture.modules.account.form.SignUpForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor // private final 타입의 멤버 variable 을 생성자를 만들어준다.
public class SignUpFormValidator implements Validator {

    private final AccountRepository accountRepository;

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.isAssignableFrom(SignUpForm.class); // SignUpForm 타입의 인스턴스를 검사
    }

    @Override
    public void validate(Object target, Errors errors) {
        // TODO email, nickname db에서 조회해서 중복 여부 검사
//        SignUpForm signUpForm = (SignUpForm) errors; // 에러
        SignUpForm signUpForm = (SignUpForm) target; // target이 폼에서 들어오는 객체를 담고 있고, errors는 에러 담고 있는 거
        if (accountRepository.existsByEmail(signUpForm.getEmail())) {
            errors.rejectValue("email", "invalid.email", new Object[]{signUpForm.getEmail()}, "이미 사용중인 이메일입니다.");
        }

        if (accountRepository.existsByNickname(signUpForm.getNickname())) {
            errors.rejectValue("nickname", "invalid.nickname", new Object[]{signUpForm.getNickname()}, "이미 사용중인 닉네임입니다.");
        }

    } // validate
}
