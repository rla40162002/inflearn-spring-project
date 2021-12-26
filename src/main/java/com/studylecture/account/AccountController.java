package com.studylecture.account;

import com.studylecture.domain.Account;
import com.studylecture.account.form.SignUpForm;
import com.studylecture.account.validator.SignUpFormValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;

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

        Account account = accountService.processNewAccount(signUpForm);
        accountService.login(account);
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


        if (!account.isValidToken(token)) {
            model.addAttribute("error", "wrong.token");
            return view;
        }
        // 계정이 존재하고 토큰 인증이 완료된 후,

        accountService.completeSignup(account);

        // 넘겨받는 폼에서 필요한 정보들  ~~번째 가입, ~~님
        model.addAttribute("numberOfUser", accountRepository.count()); // 유저 수
        model.addAttribute("nickname", account.getNickname()); // 닉네임
        return view;

    } // checkEmailToken

    @GetMapping("/check-email")
    public String checkEmail(@CurrentUser Account account, Model model) {
        model.addAttribute("email", account.getEmail());
        return "account/check-email";
    }


    @GetMapping("/resend-confirm-email")
    public String resendConfirmEmail(@CurrentUser Account account, Model model) {
        if (!account.canSendConfirmEmail()) {
            model.addAttribute("error", "인증 이메일은 1시간에 한 번만 전송할 수 있습니다.");
            model.addAttribute("email", account.getEmail());
            return "account/check-email";
        }
        accountService.sendSignUpConfirmEmail(account);
        return "redirect:/";
    } // resendConfirmEmail

    @GetMapping("/profile/{nickname}")
    public String viewProfile(@PathVariable String nickname, Model model, @CurrentUser Account account) {
        Account byNickname = accountRepository.findByNickname(nickname);
        if (byNickname == null) {
            throw new IllegalArgumentException(nickname + "에 해당하는 사용자가 없습니다.");
        }
        model.addAttribute(byNickname); // account 라는 키값으로 들어간다. 들어가는 타입의 camelCase 로 들어간다.
        model.addAttribute("isOwner", byNickname.equals(account)); // 같은 객체면 owner
        return "account/profile";
    } // viewProfile

    @GetMapping("/email-login")
    public String emailLoginForm() {
        return "account/email-login";
    } // emailLoginForm

    @PostMapping("/email-login")
    public String sendEmailLoginLink(String email, Model model, RedirectAttributes attributes) {

        Account account = accountRepository.findByEmail(email);

        if (account == null) { // 이메일이 존재하지 않으면
            model.addAttribute("error", "유효한 이메일 주소가 아닙니다.");
            return "account/email-login";
        }

        if (!account.canSendConfirmEmail()) { // 1시간이 지나기 전에 다시 요청하면
            model.addAttribute("error", "이메일 로그인 발송 요청은 1시간에 한 번만 가능합니다.");
            return "account/email-login";
        }

        accountService.sendLoginLink(account);
        attributes.addFlashAttribute("message", "이메일 로그인 메일을 발송했습니다.");
        return "redirect:/email-login";
    } // sendEmailLoginLink

    @GetMapping("/login-by-email")
    public String loginByEmail(String token, String email, Model model) {
        Account account = accountRepository.findByEmail(email);
        String view = "account/logged-in-by-email";

        if (account == null || !token.equals(account.getEmailCheckToken())) {
            // 이메일이 존재하지 않거나, 토큰이 일치하지 않으면
            model.addAttribute("error", "로그인할 수 없습니다.");
            return view;
        } // if

        accountService.login(account);
        return view;
    } // loginByEmail
}
