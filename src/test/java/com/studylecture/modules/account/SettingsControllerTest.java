package com.studylecture.modules.account;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studylecture.infra.AbstractContainerBaseTest;
import com.studylecture.infra.MockMvcTest;
import com.studylecture.modules.tag.Tag;
import com.studylecture.modules.tag.TagForm;
import com.studylecture.modules.tag.TagRepository;
import com.studylecture.modules.zone.Zone;
import com.studylecture.modules.zone.ZoneForm;
import com.studylecture.modules.zone.ZoneRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static com.studylecture.modules.account.SettingsController.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@MockMvcTest
class SettingsControllerTest extends AbstractContainerBaseTest {


    @Autowired
    MockMvc mockMvc;
    @Autowired
    AccountRepository accountRepository;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    TagRepository tagRepository;
    @Autowired
    AccountService accountService;
    @Autowired
    ZoneRepository zoneRepository;

    private Zone testZone = Zone.builder().city("test").localNameOfCity("테스트시").province("테스트주").build();

    @BeforeEach
    void beforeEach() {
        zoneRepository.save(testZone);
    }

    @AfterEach
    void afterEach() {
        accountRepository.deleteAll();
        zoneRepository.deleteAll();
    }

    @WithAccount("kyw")
    @DisplayName("프로필 수정 폼")
    @Test
    void updateProfileForm() throws Exception {
        String bio = "짧은 소개를 수정하는 경우.";
        mockMvc.perform(get(SettingsController.ROOT + SETTINGS + PROFILE))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("profile"))
                .andExpect(model().attributeExists("account"));

    } // updateProfileForm

    @WithAccount("kyw")
    @DisplayName("프로필 수정하기 - 입력값 정상")
    @Test
    void updateProfile() throws Exception {
        String bio = "짧은 소개를 수정하는 경우.";
        mockMvc.perform(post(ROOT + SETTINGS + PROFILE)
                        .param("bio", bio)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(ROOT + SETTINGS + PROFILE))
                .andExpect(flash().attributeExists("message"));

        Account kyw = accountRepository.findByNickname("kyw");
        assertEquals(bio, kyw.getBio());
    } // updateProfile


    @WithAccount("kyw")
    @DisplayName("프로필 수정하기 - 입력값 에러")
    @Test
    void updateProfileError() throws Exception {
        String bio = "길게 소개를 수정하는 경우, 길게 소개를 수정하는 경우, 길게 소개를 수정하는 경우, 길게 길게 길게 소개를 수정하는 경우";
        mockMvc.perform(post(ROOT + SETTINGS + PROFILE)
                        .param("bio", bio)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name(SETTINGS + PROFILE))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("profile"))
                .andExpect(model().hasErrors());

        Account kyw = accountRepository.findByNickname("kyw");
        assertNull(kyw.getBio());
    } // updateProfileError


    @WithAccount("kyw")
    @DisplayName("패스워드 수정 폼")
    @Test
    void updatePasswordForm() throws Exception {
        mockMvc.perform(get(ROOT + SETTINGS + PASSWORD))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("passwordForm"));
    } // updatePasswordForm

    @WithAccount("kyw")
    @DisplayName("패스워드 수정 - 입력값 정상")
    @Test
    void updatePasswordSuccess() throws Exception {
        mockMvc.perform(post(ROOT + SETTINGS + PASSWORD)
                        .param("newPassword", "12345678")
                        .param("newPasswordConfirm", "12345678")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(ROOT + SETTINGS + PASSWORD))
                .andExpect(flash().attributeExists("message"));

        Account kyw = accountRepository.findByNickname("kyw");
        assertTrue(passwordEncoder.matches("12345678", kyw.getPassword()));
    } // updatePasswordSuccess

    @WithAccount("kyw")
    @DisplayName("패스워드 수정 - 입력값 에러 - 패스워드 불일치")
    @Test
    void updatePasswordFail() throws Exception {
        mockMvc.perform(post(ROOT + SETTINGS + PASSWORD)
                        .param("newPassword", "12345678")
                        .param("newPasswordConfirm", "11111111")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name(SETTINGS + PASSWORD))
                .andExpect(model().attributeExists("passwordForm"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().hasErrors());
    } // updatePasswordFail

    @WithAccount("kyw")
    @DisplayName("닉네임 변경 폼")
    @Test
    void updateNicknameForm() throws Exception {
        mockMvc.perform(get(ROOT + SETTINGS + ACCOUNT))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("nicknameForm"));

    } // updateNicknameForm

    @WithAccount("kyw")
    @DisplayName("닉네임 변경 - 성공")
    @Test
    void updateNicknameSuccess() throws Exception {
        String newNickname = "qkoo";
        mockMvc.perform(post(ROOT + SETTINGS + ACCOUNT)
                        .param("nickname", newNickname)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(ROOT + SETTINGS + ACCOUNT))
                .andExpect(flash().attributeExists("message"));

        assertNotNull(accountRepository.findByNickname("qkoo"));
    } // updateNicknameSuccess

    @WithAccount("kyw")
    @DisplayName("닉네임 변경 - 입력값 에러")
    @Test
    void updateNicknameFail() throws Exception {
        String newNickname = "¯;_(ツ)_/¯";
        mockMvc.perform(post(ROOT + SETTINGS + ACCOUNT)
                        .param("nickname", newNickname)
                        .with(csrf()))
                .andExpect(status().isOk()) // why 400? => @Valid 후 매개변수 순서.. valid할 폼과 error
                .andExpect(view().name(SETTINGS + ACCOUNT))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("nicknameForm"));

    } // updateNicknameFail

    @WithAccount("kyw")
    @DisplayName("특정 계정 태그 수정 폼")
    @Test
    void updateTagForm() throws Exception {
        mockMvc.perform(get(ROOT + SETTINGS + TAGS)
                ).andExpect(view().name(SETTINGS + TAGS))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("whitelist"))
                .andExpect(model().attributeExists("tags"));
    } // updateTagForm

    @WithAccount("kyw")
    @DisplayName("특정 계정 태그 추가")
    @Test
    void addTag() throws Exception {
        TagForm tagForm = new TagForm();
        tagForm.setTagTitle("newTag");

        mockMvc.perform(post(ROOT + SETTINGS + TAGS + "/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tagForm))
                        .with(csrf()))
                .andExpect(status().isOk());

        Tag newTag = tagRepository.findByTitle(tagForm.getTagTitle());
        assertNotNull(newTag);
        Account kyw = accountRepository.findByNickname("kyw"); // 가져온 객체의 상태가 detached 상태이다
        assertTrue(kyw.getTags().contains(newTag));
    } // addTag

    @WithAccount("kyw")
    @DisplayName("특정 계정 태그 삭제")
    @Test
    void removeTag() throws Exception {
        Account kyw = accountRepository.findByNickname("kyw"); // 가져온 객체의 상태가 detached 상태이다
        Tag newTag = tagRepository.save(Tag.builder().title("newTag").build());
        accountService.addTag(kyw, newTag);
        assertTrue(kyw.getTags().contains(newTag));
        // 먼저 만들고 있어야 함.

        TagForm tagForm = new TagForm();
        tagForm.setTagTitle("newTag");

        mockMvc.perform(post(ROOT + SETTINGS + TAGS + "/remove")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tagForm))
                        .with(csrf()))
                .andExpect(status().isOk());
        assertFalse(kyw.getTags().contains(newTag));
    } // removeTag

    @WithAccount("kyw")
    @DisplayName("특정 계정 지역 정보 수정 폼")
    @Test
    void updateZoneForm() throws Exception {
        mockMvc.perform(get(ROOT + SETTINGS + ZONES)
                ).andExpect(view().name(SETTINGS + ZONES))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("whitelist"))
                .andExpect(model().attributeExists("zones"));
    } // updateZoneForm

    @WithAccount("kyw")
    @DisplayName("특정 계정 지역 정보 추가")
    @Test
    void addZone() throws Exception {
        ZoneForm zoneForm = new ZoneForm();
        zoneForm.setZoneName(testZone.toString());

        mockMvc.perform(post(ROOT + SETTINGS + ZONES + "/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(zoneForm))
                        .with(csrf()))
                .andExpect(status().isOk());

        Account kyw = accountRepository.findByNickname("kyw"); // 가져온 객체의 상태가 detached 상태이다
        Zone zone = zoneRepository.findByCityAndProvince(testZone.getCity(), testZone.getProvince());
        assertTrue(kyw.getZones().contains(zone));
    } // addZone

    @WithAccount("kyw")
    @DisplayName("특정 계정 지역 정보 삭제")
    @Test
    void removeZone() throws Exception {
        Account kyw = accountRepository.findByNickname("kyw"); // 가져온 객체의 상태가 detached 상태이다
        Zone zone = zoneRepository.findByCityAndProvince(testZone.getCity(), testZone.getProvince());
        accountService.addZone(kyw, zone);

        ZoneForm zoneForm = new ZoneForm();
        zoneForm.setZoneName(testZone.toString());

        mockMvc.perform(post(ROOT + SETTINGS + ZONES + "/remove")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(zoneForm))
                        .with(csrf()))
                .andExpect(status().isOk());

        assertFalse(kyw.getTags().contains(zone));
    } // removeZone

}