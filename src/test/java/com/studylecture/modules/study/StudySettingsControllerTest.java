package com.studylecture.modules.study;

import com.studylecture.infra.AbstractContainerBaseTest;
import com.studylecture.infra.MockMvcTest;
import com.studylecture.modules.account.Account;
import com.studylecture.modules.account.AccountFactory;
import com.studylecture.modules.account.AccountRepository;
import com.studylecture.modules.account.WithAccount;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@MockMvcTest
class StudySettingsControllerTest extends AbstractContainerBaseTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    StudyFactory studyFactory;
    @Autowired
    AccountFactory accountFactory;
    @Autowired
    AccountRepository accountRepository;

    @WithAccount("kyw")
    @DisplayName("스터디 소개 수정 폼 조회 - 성공")
    @Test
    void updateDescriptionFormSuccess() throws Exception {
        Account kyw = accountRepository.findByNickname("kyw"); // 로그인 한 유저이자 매니저
        Study study = studyFactory.createStudy("test-study", kyw);


        mockMvc.perform(get("/study/" + study.getPath() + "/settings/description"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("study"))
                .andExpect(model().attributeExists("studyDescriptionForm"))
                .andExpect(view().name("study/settings/description"));

    } // 스터디 소개 수정 폼 조회 - 성공

    @WithAccount("kyw")
    @DisplayName("스터디 소개 수정 폼 조회 - 실패(권한 없음)")
    @Test
    void updateDescriptionFormFail() throws Exception {
        Account kyw1023 = accountFactory.createAccount("kyw1023"); // 얘가 메니저
        Study study = studyFactory.createStudy("test-study", kyw1023);

        mockMvc.perform(get("/study/" + study.getPath() + "/settings/description"))
//                .andExpect(status().isForbidden());
                .andExpect(status().isOk())
                .andExpect(view().name("error"));
    } // 스터디 소개 수정 폼 조회 - 실패(권한 없음)

    @WithAccount("kyw")
    @DisplayName("스터디 소개 수정 - 성공")
    @Test
    void updateDescriptionSuccess() throws Exception {
        Account kyw = accountRepository.findByNickname("kyw"); // 로그인 한 유저이자 매니저
        Study study = studyFactory.createStudy("test-study", kyw);

        mockMvc.perform(post("/study/" + study.getPath() + "/settings/description")
                        .param("shortDescription", "short Description")
                        .param("fullDescription", "full description")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/" + study.getPath() + "/settings/description"))
                .andExpect(flash().attributeExists("message"));

    } // 스터디 소개 수정 - 성공

    @WithAccount("kyw")
    @DisplayName("스터디 소개 수정 - 실패")
    @Test
    void updateDescriptionFail() throws Exception {
        Account kyw = accountRepository.findByNickname("kyw"); // 로그인 한 유저이자 매니저
        Study study = studyFactory.createStudy("test-study", kyw);

        mockMvc.perform(post("/study/" + study.getPath() + "/settings/description")
                        .param("shortDescription", "")
                        .param("fullDescription", "full description")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("study"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("studyDescriptionForm"));

    } // 스터디 소개 수정 - 실패


}
