package com.studylecture.study;

import com.studylecture.WithAccount;
import com.studylecture.account.AccountRepository;
import com.studylecture.domain.Account;
import com.studylecture.domain.Study;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
@RequiredArgsConstructor
class StudyControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    StudyRepository studyRepository;
    @Autowired
    StudyService studyService;
    @Autowired
    AccountRepository accountRepository;

    @AfterEach
    void afterEach() {
        accountRepository.deleteAll();
    }

    @Test
    @WithAccount("kyw")
    @DisplayName("스터디 개설 폼 조회")
    void createStudyForm() throws Exception {
        mockMvc.perform(get("/new-study"))
                .andExpect(status().isOk())
                .andExpect(view().name("study/form"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("studyForm"));
    } // createStudyForm

    @WithAccount("kyw")
    @DisplayName("스터디 개설 - 성공")
    @Test
    void createStudySuccess() throws Exception {
        mockMvc.perform(post("/new-study")
                        .param("path", "test-path")
                        .param("title", "test-title")
                        .param("shortDescription", "short description test")
                        .param("fullDescription", "full description test")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/test-path"));

        Study study = studyRepository.findByPath("test-path");
        assertNotNull(study);
        Account account = accountRepository.findByNickname("kyw");
        assertTrue(study.getManagers().contains(account));

    } // 스터디 개설 성공

    @WithAccount("kyw")
    @DisplayName("스터디 개설 - 실패")
    @Test
    void createStudyFail() throws Exception {
        mockMvc.perform(post("/new-study")
                        .param("path", "wrong path") // 공백
                        .param("title", "test-title")
                        .param("shortDescription", "short description test")
                        .param("fullDescription", "full description test")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("study/form"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("studyForm"));
        Study study = studyRepository.findByPath("wrong path");
        assertNull(study);
    } // 스터디 개설 성공

    @WithAccount("kyw")
    @DisplayName("스터디 조회")
    @Test
    void viewStudy() throws Exception {
        Study study = new Study();
        study.setPath("test-path");
        study.setTitle("test-title");
        study.setShortDescription("short des");
        study.setFullDescription("<p>full des</p>");

        Account kyw = accountRepository.findByNickname("kyw");
        studyService.createNewStudy(study, kyw);

        mockMvc.perform(get("/study/" + study.getPath()))
                .andExpect(status().isOk())
                .andExpect(view().name("study/view"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("study"));
    } // 스터디 조회


    @WithAccount("kyw")
    @DisplayName("스터디 구성원 조회")
    @Test
    void inquiryMember() throws Exception{
        Study study = new Study();
        study.setPath("test-path");
        study.setTitle("test-title");
        study.setShortDescription("short des");
        study.setFullDescription("<p>full des</p>");
        Account kyw = accountRepository.findByNickname("kyw");
        studyService.createNewStudy(study, kyw);

        mockMvc.perform(get("/study/" + study.getPath() + "/members"))
                .andExpect(view().name("study/members"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("study"));
    } // 스터디 구성원 조회
}