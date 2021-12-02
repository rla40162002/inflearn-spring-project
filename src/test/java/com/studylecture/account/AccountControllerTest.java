package com.studylecture.account;

import com.studylecture.domain.Account;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountRepository accountRepository;

    @MockBean
    JavaMailSender javaMailSender;

    @Test
    @DisplayName("회원가입 화면 보이는지 테스트")
    void signUpForm() throws Exception {
        mockMvc.perform(get("/sign-up"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("account/sign-up"))
                .andExpect(model().attributeExists("signUpForm"));
    }

    @Test
    @DisplayName("회원 가입 처리 - 입력값 오류")
    void signUpSubmitWithWrongError() throws Exception {
        mockMvc.perform(post("/sign-up")
                        .param("nickname", "kyw")
                        .param("email", "email.1")
                        .param("password", "123")
                        .with(csrf())
                )
                .andExpect(status().isOk())
                .andExpect(view().name("account/sign-up"));
    } // 오류

    @Test
    @DisplayName("회원 가입 처리 - 입력값 정상")
    void signUpSubmitWithCorrectInput() throws Exception {
        mockMvc.perform(post("/sign-up")
                        .param("nickname", "kyw")
                        .param("email", "rla4062002@naver.com")
                        .param("password", "12345678")
                        .with(csrf())
                )
                .andExpect(status().is3xxRedirection()) // 리다이렉션 응답
                .andExpect(view().name("redirect:/"));

        Account account = accountRepository.findByEmail("rla4062002@naver.com");


        assertNotNull(account);
        assertNotEquals(account.getPassword(), "12345678");
        assertNotNull(account.getEmailCheckToken());
        assertTrue(accountRepository.existsByEmail("rla4062002@naver.com")); // 이메일 중복여부
        then(javaMailSender).should().send(any(SimpleMailMessage.class)); // SimpleMailMessage타입의 아무거나라도 들어왔는가 확인
    }


}