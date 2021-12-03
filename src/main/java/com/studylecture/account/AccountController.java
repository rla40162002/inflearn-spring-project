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
import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
public class AccountController {
    private final SignUpFormValidator signUpFormValidator;
    private final AccountService accountService;
    private final AccountRepository accountRepository;

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

        accountService.processNewAccount(signUpForm);

        // TODO: 회원가입 처리
        return "redirect:/";
    } // signUpSubmit

    @GetMapping("/check-email-token")
    public String checkEmailToken(String token, String email, Model model) {
        Account account = accountRepository.findByEmail(email); // 이메일 찾아오고
//        String view = "account/checked-email"; // 공통되는 부분
        String view = "account/checked-email";
        if (account == null) { // 조회되는 게 없을 때
            model.addAttribute("error", "wrong.email");
            return view;
        }

        if (!account.getEmailCheckToken().equals(token)) { // 토큰과 맞지 않을 때
            model.addAttribute("error", "wrong.token");
            return view;
        }
        // 계정이 존재하고 토큰 인증이 완료된 후,

      account.completeSignUp(); // verified true, joinedAt now
        
        // 넘겨받는 폼에서 필요한 정보들  ~~번째 가입, ~~님
        model.addAttribute("numberOfUser", accountRepository.count()); // 유저 수
        model.addAttribute("nickname", account.getNickname()); // 닉네임
        return view;

    } // checkEmailToken

}
