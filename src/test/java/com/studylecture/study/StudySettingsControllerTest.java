package com.studylecture.study;

import com.studylecture.WithAccount;
import com.studylecture.domain.Account;
import com.studylecture.domain.Study;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@RequiredArgsConstructor
public class StudySettingsControllerTest extends StudyControllerTest {


    @WithAccount("kyw")
    @DisplayName("스터디 소개 수정 폼 조회 - 성공")
    @Test
    void updateDescriptionFormSuccess() throws Exception {
        Account kyw = accountRepository.findByNickname("kyw"); // 로그인 한 유저이자 매니저
        Study study = createStudy("test-study", kyw);


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
        Account kyw1023 = createAccount("kyw1023"); // 얘가 메니저
        Study study = createStudy("test-study", kyw1023);

        mockMvc.perform(get("/study/" + study.getPath() + "/settings/description"))
                .andExpect(status().isForbidden());
    } // 스터디 소개 수정 폼 조회 - 실패(권한 없음)

    @WithAccount("kyw")
    @DisplayName("스터디 소개 수정 - 성공")
    @Test
    void updateDescriptionSuccess() throws Exception {
        Account kyw = accountRepository.findByNickname("kyw"); // 로그인 한 유저이자 매니저
        Study study = createStudy("test-study", kyw);

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
        Study study = createStudy("test-study", kyw);

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
