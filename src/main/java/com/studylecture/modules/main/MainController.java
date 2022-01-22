package com.studylecture.modules.main;

import com.studylecture.modules.account.CurrentAccount;
import com.studylecture.modules.account.Account;
import com.studylecture.modules.study.Study;
import com.studylecture.modules.study.StudyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final StudyRepository studyRepository;

    @GetMapping("/")
    public String home(@CurrentAccount Account account, Model model) {
        if (account != null) {
            model.addAttribute(account); // null이 아니면 로그인 한 것이므로 계정 넘겨줌
        }
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/search/study")
    public String searchStudy(String keyword, Model model,
                              @PageableDefault(size = 9, sort = "publishedDateTime", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<Study> studyPage = studyRepository.findByKeyword(keyword, pageable);

        model.addAttribute("keyword", keyword);
        model.addAttribute("studyPage", studyPage);

        return "search";
    } // searchStudy

}
