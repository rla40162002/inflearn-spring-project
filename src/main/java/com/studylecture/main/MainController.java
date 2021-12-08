package com.studylecture.main;

import com.studylecture.account.CurrentUser;
import com.studylecture.domain.Account;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

    @GetMapping("/")
    public String home(@CurrentUser Account account, Model model) {
        if (account != null) {
            model.addAttribute(account); // null이 아니면 로그인 한 것이므로 계정 넘겨줌
        }
        return "index";
    }
}
