package com.studylecture.settings;

import com.studylecture.account.CurrentUser;
import com.studylecture.domain.Account;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class SettingController {

    @GetMapping("/settings/profile")
    public String profileUpdateForm(@CurrentUser Account account, Model model) {

        model.addAttribute(account);
        model.addAttribute(new Profile(account));

        return "settings/profile";
    }

    @PostMapping("/settings/profile")
    public String updateProfile(){

        return "settings/profile";
    }

}
