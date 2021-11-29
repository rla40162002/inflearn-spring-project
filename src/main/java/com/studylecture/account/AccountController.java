package com.studylecture.account;

import com.studylecture.domain.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;

@Controller
@RequiredArgsConstructor
public class AccountController {
    private final SignUpFormValidator signUpFormValidator;
    private final AccountRepository accountRepository;
    private final JavaMailSender javaMailSender;

    @InitBinder("signUpForm") // 33번째줄 변수명이 아닌 클래스명을 따라간다.
    public void initBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(signUpFormValidator);
    }

    @GetMapping("/sign-up")
    public String signUpForm(Model model) {
        model.addAttribute(new SignUpForm());
        return "account/sign-up";
    }

    @PostMapping("/sign-up")
    public String signUpSubmit(@Valid SignUpForm signUpForm, Errors errors) { // @ModelAttribute 생략가능
        if (errors.hasErrors()) { // 백엔드 검사하는 부분
            return "account/sign-up";
        }

    /*       signUpFormValidator.validate(signUpForm, errors);
        if(errors.hasErrors()){
            return "account/sign-up"; // initBinder 로 대체
        }*/

        Account account = Account.builder()
                .email(signUpForm.getEmail())
                .nickname(signUpForm.getNickname())
                .password(signUpForm.getPassword())   //TODO encoding 해야함
                .studyCreatedByWeb(true)
                .studyUpdatedByWeb(true)
                .studyJoinResultByWeb(true) // web 알림만 켜두기
                .build();

        Account newAccount = accountRepository.save(account);

        // 토큰 만들어서 메시지에 담고 보내는 부분
        newAccount.generateEmailCheckToken();
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(newAccount.getEmail()); // 받는 사람
        mailMessage.setSubject("스터디 사이트 연습, 회원 가입 인증"); // 제목
        mailMessage.setText("/check-email-token?token=" + newAccount.getEmailCheckToken()
                + "&email=" + newAccount.getEmail()); // 내용
        javaMailSender.send(mailMessage);

        // TODO: 회원가입 처리
        return "redirect:/";

    } // signUpSubmit
}
