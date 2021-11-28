package com.studylecture.account;

import lombok.RequiredArgsConstructor;
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
            return "account/sign-up";
        }*/

        // TODO: 회원가입 처리
        return "redirect:/";

    } // signUpSubmit
}
