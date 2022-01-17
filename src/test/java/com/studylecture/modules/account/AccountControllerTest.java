package com.studylecture.modules.account;

import com.studylecture.infra.AbstractContainerBaseTest;
import com.studylecture.infra.MockMvcTest;
import com.studylecture.infra.mail.EmailMessage;
import com.studylecture.infra.mail.EmailService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@MockMvcTest
class AccountControllerTest extends AbstractContainerBaseTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountRepository accountRepository;

    @MockBean
    EmailService emailService;

    @Test
    @DisplayName("회원가입 화면 보이는지 테스트")
    void signUpForm() throws Exception {
        mockMvc.perform(get("/sign-up"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("account/sign-up"))
                .andExpect(model().attributeExists("signUpForm"))
                .andExpect(unauthenticated());
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
                .andExpect(view().name("account/sign-up"))
                .andExpect(unauthenticated());
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
                .andExpect(view().name("redirect:/"))
                .andExpect(authenticated().withUsername("kyw"));
        ;

        Account account = accountRepository.findByEmail("rla4062002@naver.com");


        assertNotNull(account);
        assertNotEquals(account.getPassword(), "12345678");
        assertNotNull(account.getEmailCheckToken());
        assertTrue(accountRepository.existsByEmail("rla4062002@naver.com")); // 이메일 중복여부
        then(emailService).should().sendEmail(any(EmailMessage.class));// EmailMessage 타입의 아무거나라도 들어왔는가 확인
    }

    @Test
    @DisplayName("인증 메일 확인 - 입력값 오류")
    void checkEmailTokenWithWrongInput() throws Exception {
        mockMvc.perform(get("/check-email-token")
                        .param("token", "sdfsdfsdf")
                        .param("email", "rla@nav.com"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("error"))
                .andExpect(view().name("account/checked-email"))
                .andExpect(unauthenticated());

    }

    @Test
    @DisplayName("인증 메일 확인 - 입력값 정상")
    void checkEmailToken() throws Exception {
        Account account = Account.builder()
                .email("test@ntest.com")
                .password("12345678")
                .nickname("kyw")
                .build();
        Account newAccount = accountRepository.save(account);
        newAccount.generateEmailCheckToken();
        mockMvc.perform(get("/check-email-token")
                        .param("token", newAccount.getEmailCheckToken())
                        .param("email", newAccount.getEmail()))
                .andExpect(status().isOk())
                .andExpect(model().attributeDoesNotExist("error"))
                .andExpect(model().attributeExists("nickname"))
                .andExpect(model().attributeExists("numberOfUser"))
                .andExpect(view().name("account/checked-email"))
                .andExpect(authenticated().withUsername("kyw"));
    }
}