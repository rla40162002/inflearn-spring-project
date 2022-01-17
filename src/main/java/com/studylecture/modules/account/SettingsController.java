package com.studylecture.modules.account;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studylecture.modules.account.form.NicknameForm;
import com.studylecture.modules.account.form.Notifications;
import com.studylecture.modules.account.form.PasswordForm;
import com.studylecture.modules.account.form.Profile;
import com.studylecture.modules.tag.Tag;
import com.studylecture.modules.zone.Zone;
import com.studylecture.modules.account.validator.NicknameValidator;
import com.studylecture.modules.account.validator.PasswordFormValidator;
import com.studylecture.modules.tag.TagForm;
import com.studylecture.modules.tag.TagRepository;
import com.studylecture.modules.tag.TagService;
import com.studylecture.modules.zone.ZoneForm;
import com.studylecture.modules.zone.ZoneRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.studylecture.modules.account.SettingsController.ROOT;
import static com.studylecture.modules.account.SettingsController.SETTINGS;

@Controller
@RequiredArgsConstructor
@RequestMapping(ROOT + SETTINGS)
public class SettingsController {
    private final AccountService accountService;
    private final ModelMapper modelMapper;
    private final NicknameValidator nicknameValidator;
    private final TagRepository tagRepository;
    private final ObjectMapper objectMapper;
    private final ZoneRepository zoneRepository;
    private final TagService tagService;

    @InitBinder("passwordForm")
    public void passwordFormInitBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(new PasswordFormValidator());
    }

    @InitBinder("nicknameForm")
    public void nicknameFormInitBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(nicknameValidator);
    }

    static final String ROOT = "/";
    static final String SETTINGS = "settings";
    static final String PROFILE = "/profile";
    static final String PASSWORD = "/password";
    static final String NOTIFICATIONS = "/notifications";
    static final String ACCOUNT = "/account";
    static final String TAGS = "/tags";
    static final String ZONES = "/zones";

    @GetMapping(PROFILE)
    public String updateProfileForm(@CurrentAccount Account account, Model model) {

        model.addAttribute(account);
        model.addAttribute(modelMapper.map(account, Profile.class));
        // account에 들어있는 정보를 갖고 Profile 타입의 객체를 만든다.

        return SETTINGS + PROFILE;
    } // updateProfileForm

    @PostMapping(PROFILE)
    public String updateProfile(@CurrentAccount Account account, @Valid @ModelAttribute Profile profile, Errors errors,
                                Model model, RedirectAttributes attributes) {
        if (errors.hasErrors()) {
            model.addAttribute(account);
            return SETTINGS + PROFILE;
        }

        accountService.updateProfile(account, profile);
        attributes.addFlashAttribute("message", "프로필을 수정했습니다.");

        return "redirect:/" + SETTINGS + PROFILE;
    } // updateProfile

    @GetMapping(PASSWORD)
    public String updatePasswordForm(@CurrentAccount Account account, Model model) {
        model.addAttribute(account);
        model.addAttribute(new PasswordForm());
        return SETTINGS + PASSWORD;
    } // updatePasswordForm

    @PostMapping(PASSWORD)
    public String updatePassword(@CurrentAccount Account account, @Valid PasswordForm passwordForm, Errors errors,
                                 Model model, RedirectAttributes attributes) {
        if (errors.hasErrors()) {
            model.addAttribute(account);
            return SETTINGS + PASSWORD;
        }

        accountService.updatePassword(account, passwordForm.getNewPassword());

        attributes.addFlashAttribute("message", "패스워드를 변경했습니다.");
        return "redirect:/" + SETTINGS + PASSWORD;
    } // updatePassword

    @GetMapping(NOTIFICATIONS)
    public String updateNotificationsForm(@CurrentAccount Account account, Model model) {
        model.addAttribute(account);
        model.addAttribute(modelMapper.map(account, Notifications.class));

        return SETTINGS + NOTIFICATIONS;
    } // updateNotificationsForm

    @PostMapping(NOTIFICATIONS)
    public String updateNotifications(@CurrentAccount Account account, @Valid Notifications notifications, Errors errors,
                                      Model model, RedirectAttributes attributes) {
        if (errors.hasErrors()) {
            model.addAttribute(account);
            return SETTINGS + NOTIFICATIONS;
        }

        accountService.updateNotifications(account, notifications);
        attributes.addFlashAttribute("message", "알림 설정을 변경했습니다.");
        return "redirect:/" + SETTINGS + NOTIFICATIONS;
    } // updateNotifications

    @GetMapping(ACCOUNT)
    public String updateAccountForm(@CurrentAccount Account account, Model model) {
        model.addAttribute(account);
        model.addAttribute(modelMapper.map(account, NicknameForm.class));
        return SETTINGS + ACCOUNT;
    } // updateAccountForm

    @PostMapping(ACCOUNT)
    public String updateAccount(@CurrentAccount Account account, @Valid NicknameForm nicknameForm, Errors errors,
                                Model model, RedirectAttributes attributes) {

        if (errors.hasErrors()) {
            model.addAttribute(account);
            return SETTINGS + ACCOUNT;
        }

        accountService.updateNickname(account, nicknameForm.getNickname());
        attributes.addFlashAttribute("message", "닉네임을 수정했습니다.");

        return "redirect:/" + SETTINGS + ACCOUNT;
    } // updateAccount

    @GetMapping(TAGS)
    public String updateTags(@CurrentAccount Account account, Model model) throws JsonProcessingException {
        Set<Tag> tags = accountService.getTags(account);
        model.addAttribute(account);
        model.addAttribute("tags", tags.stream().map(Tag::getTitle).collect(Collectors.toList()));

        List<String> allTags = tagRepository.findAll().stream().map(Tag::getTitle).collect(Collectors.toList());
        model.addAttribute("whitelist", objectMapper.writeValueAsString(allTags));

        return SETTINGS + TAGS;
    } // updateTags

    @PostMapping(TAGS + "/add")
    @ResponseBody
    public ResponseEntity addTag(@CurrentAccount Account account, @RequestBody TagForm tagForm) {
        Tag tag = tagService.findOrCreateNew(tagForm.getTagTitle());
        accountService.addTag(account, tag);

        return ResponseEntity.ok().build();
    } // addTag

    @PostMapping(TAGS + "/remove")
    public ResponseEntity removeTag(@CurrentAccount Account account, @RequestBody TagForm tagForm) {
        String title = tagForm.getTagTitle();
        Tag tag = tagRepository.findByTitle(title);

        if (tag == null) { // 없는 태그를 삭제하려고 할 때(보내온 요청 자체가 잘못된 것)
            return ResponseEntity.badRequest().build();
        }

        accountService.removeTag(account, tag);

        return ResponseEntity.ok().build();
    } // removeTag

    @GetMapping(ZONES)
    public String updateZonesForm(@CurrentAccount Account account, Model model) throws JsonProcessingException {

        Set<Zone> zones = accountService.getZones(account);
        List<String> allZones = zoneRepository.findAll().stream().map(Zone::toString).collect(Collectors.toList());

        model.addAttribute(account);
        model.addAttribute("zones", zones.stream().map(Zone::toString).collect(Collectors.toList()));
        model.addAttribute("whitelist", objectMapper.writeValueAsString(allZones));

        return SETTINGS + ZONES;
    } // updateZoneForm

    @PostMapping(ZONES + "/add")
    @ResponseBody
    public ResponseEntity addZone(@CurrentAccount Account account, @RequestBody ZoneForm zoneForm) {
        Zone zone = zoneRepository.findByCityAndProvince(zoneForm.getCityName(), zoneForm.getProvinceName());

        if (zone == null) {
            return ResponseEntity.badRequest().build();
        }

        accountService.addZone(account, zone);

        return ResponseEntity.ok().build();
    } // addZone

    @PostMapping(ZONES + "/remove")
    @ResponseBody
    public ResponseEntity removeZone(@CurrentAccount Account account, @RequestBody ZoneForm zoneForm) {
        Zone zone = zoneRepository.findByCityAndProvince(zoneForm.getCityName(), zoneForm.getProvinceName());

        if (zone == null) {
            return ResponseEntity.badRequest().build();
        }

        accountService.removeZone(account, zone);
        return ResponseEntity.ok().build();
    } // removeZone

}
