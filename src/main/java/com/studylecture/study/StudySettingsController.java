package com.studylecture.study;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studylecture.account.CurrentAccount;
import com.studylecture.domain.Account;
import com.studylecture.domain.Study;
import com.studylecture.domain.Tag;
import com.studylecture.domain.Zone;
import com.studylecture.tag.TagForm;
import com.studylecture.study.form.StudyDescriptionForm;
import com.studylecture.tag.TagRepository;
import com.studylecture.tag.TagService;
import com.studylecture.zone.ZoneForm;
import com.studylecture.zone.ZoneRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/study/{path}/settings")
@RequiredArgsConstructor
public class StudySettingsController {

    private final StudyService studyService;
    private final ModelMapper modelMapper;
    private final TagRepository tagRepository;
    private final TagService tagService;
    private final ZoneRepository zoneRepository;
    private final ObjectMapper objectMapper;

    @GetMapping("/description")
    public String viewStudySetting(@CurrentAccount Account account, @PathVariable String path, Model model) {
        Study study = studyService.getStudy(path);
        model.addAttribute(study);
        model.addAttribute(account);
        model.addAttribute(modelMapper.map(study, StudyDescriptionForm.class));

        return "study/settings/description";
    } // viewStudySetting

    @PostMapping("/description")
    public String updateStudyDescription(@CurrentAccount Account account, @PathVariable String path,
                                         @Valid StudyDescriptionForm studyDescriptionForm, Errors errors, Model model,
                                         RedirectAttributes attributes) {

        Study study = studyService.getStudyToUpdate(account, path);

        if (errors.hasErrors()) {
            model.addAttribute(account);
            model.addAttribute(study);
            return "study/settings/description";
        }

        studyService.updateStudyDescription(study, studyDescriptionForm);
        attributes.addFlashAttribute("message", "스터디 소개를 수정했습니다.");

        return "redirect:/study/" + getPath(path) + "/settings/description";
    } // updateStudyDescription

    @GetMapping("/banner")
    public String studyImageForm(@CurrentAccount Account account, @PathVariable String path, Model model) {
        Study study = studyService.getStudyToUpdate(account, path);
        model.addAttribute(account);
        model.addAttribute(study);
        return "study/settings/banner";
    }

    @PostMapping("/banner")
    public String updateStudyImage(@CurrentAccount Account account, @PathVariable String path,
                                   String image, RedirectAttributes attributes) {
        Study study = studyService.getStudyToUpdate(account, path);
        studyService.updateStudyImage(study, image);
        attributes.addFlashAttribute("message", "스터디 이미지를 수정했습니다.");
        return "redirect:/study/" + getPath(path) + "/settings/banner";
    }

    @PostMapping("/banner/enable")
    public String enableStudyBanner(@CurrentAccount Account account, @PathVariable String path) {
        Study study = studyService.getStudyToUpdate(account, path);
        studyService.enableStudyBanner(study);
        return "redirect:/study/" + getPath(path) + "/settings/banner";
    }

    @PostMapping("/banner/disable")
    public String disableStudyBanner(@CurrentAccount Account account, @PathVariable String path) {
        Study study = studyService.getStudyToUpdate(account, path);
        studyService.disableStudyBanner(study);
        return "redirect:/study/" + getPath(path) + "/settings/banner";
    }

    @GetMapping("/tags")
    public String studyTagsForm(@CurrentAccount Account account, @PathVariable String path, Model model)
            throws JsonProcessingException {
        Study studyToUpdate = studyService.getStudyToUpdate(account, path);
        List<String> studyTags = studyToUpdate.getTags().stream().map(Tag::getTitle).collect(Collectors.toList());
        List<String> allTags = tagRepository.findAll().stream().map(Tag::getTitle).collect(Collectors.toList());

        model.addAttribute(studyToUpdate);
        model.addAttribute(account);
        model.addAttribute("tags", studyTags);
        model.addAttribute("whitelist", objectMapper.writeValueAsString(allTags));

        return "study/settings/tags";
    } // studyTagsForm

    @PostMapping("/tags/add")
    @ResponseBody
    public ResponseEntity addTag(@CurrentAccount Account account, @PathVariable String path,
                                 @RequestBody TagForm tagForm) {
        Study studyToUpdate = studyService.getStudyToUpdateTag(account, path);
        Tag tag = tagService.findOrCreateNew(tagForm.getTagTitle());
        studyService.addTag(studyToUpdate, tag);

        return ResponseEntity.ok().build();
    } // addTag

    @PostMapping("/tags/remove")
    public ResponseEntity removeTag(@CurrentAccount Account account, @PathVariable String path,
                                    @RequestBody TagForm tagForm) {
        Study studyToUpdate = studyService.getStudyToUpdateTag(account, path);
        Tag tag = tagRepository.findByTitle(tagForm.getTagTitle());

        if (tag == null) { // 없는데 삭제하려고 하면
            return ResponseEntity.badRequest().build();
        }

        studyService.removeTag(studyToUpdate, tag);

        return ResponseEntity.ok().build();
    } // removeTag

    @GetMapping("/zones")
    public String studyZonesForm(@CurrentAccount Account account, @PathVariable String path, Model model)
            throws JsonProcessingException {
        Study studyToUpdate = studyService.getStudyToUpdate(account, path);
        List<String> studyZones = studyToUpdate.getZones().stream().map(Zone::toString).collect(Collectors.toList());
        List<String> allZones = zoneRepository.findAll().stream().map(Zone::toString).collect(Collectors.toList());

        model.addAttribute(account);
        model.addAttribute(studyToUpdate);
        model.addAttribute("zones", studyZones);
        model.addAttribute("whitelist", objectMapper.writeValueAsString(allZones));

        return "study/settings/zones";
    }

    @PostMapping("/zones/add")
    @ResponseBody
    public ResponseEntity addZone(@CurrentAccount Account account, @PathVariable String path,
                                  @RequestBody ZoneForm zoneForm) {
        Study studyToUpdateZone = studyService.getStudyToUpdateZone(account, path);
        Zone zone = zoneRepository.findByCityAndProvince(zoneForm.getCityName(), zoneForm.getProvinceName());

        if (zone == null) {
            return ResponseEntity.badRequest().build();
        }

        studyService.addZone(studyToUpdateZone, zone);

        return ResponseEntity.ok().build();
    } // addZone

    @PostMapping("/zones/remove")
    @ResponseBody
    public ResponseEntity removeZone(@CurrentAccount Account account, @PathVariable String path,
                                     @RequestBody ZoneForm zoneForm) {
        Study studyToUpdateZone = studyService.getStudyToUpdateZone(account, path);
        Zone zone = zoneRepository.findByCityAndProvince(zoneForm.getCityName(), zoneForm.getProvinceName());

        if (zone == null) {
            return ResponseEntity.badRequest().build();
        }

        studyService.removeZone(studyToUpdateZone, zone);
        return ResponseEntity.ok().build();
    } // removeZone

    private String getPath(String path) {
        return URLEncoder.encode(path, StandardCharsets.UTF_8);
    }

    @GetMapping("/study")
    public String studySettingForm(@CurrentAccount Account account, @PathVariable String path, Model model) {
        Study studyToUpdate = studyService.getStudyToUpdate(account, path);
        model.addAttribute(account);
        model.addAttribute(studyToUpdate);
        return "study/settings/study";
    } // studySettingForm

    @PostMapping("/study/publish")
    public String publishStudy(@CurrentAccount Account account, @PathVariable String path, RedirectAttributes attributes) {
        Study studyToUpdateStatus = studyService.getStudyToUpdateStatus(account, path);
        studyService.publish(studyToUpdateStatus);
        attributes.addFlashAttribute("message", "스터디를 공개했습니다.");
        return "redirect:/study/" + getPath(path) + "/settings/study";
    }

    @PostMapping("/study/close")
    public String closeStudy(@CurrentAccount Account account, @PathVariable String path, RedirectAttributes attributes) {
        Study studyToUpdateStatus = studyService.getStudyToUpdateStatus(account, path);
        studyService.close(studyToUpdateStatus);
        attributes.addFlashAttribute("message", "스터디를 종료했습니다.");
        return "redirect:/study/" + getPath(path) + "/settings/study";
    }

    @PostMapping("/recruit/start")
    public String startRecruit(@CurrentAccount Account account, @PathVariable String path, Model model
            , RedirectAttributes attributes) {
        Study studyToUpdateStatus = studyService.getStudyToUpdateStatus(account, path);

        if (!studyToUpdateStatus.canUpdateRecruiting()) {
            attributes.addFlashAttribute("message", "1시간 안에 인원 모집 설정을 여러 번 변경할 수 없습니다.");
            return "redirect:/study/" + getPath(path) + "/settings/study";
        }
        studyService.startRecruit(studyToUpdateStatus);
        attributes.addFlashAttribute("message", "인원 모집을 시작했습니다.");
        return "redirect:/study/" + getPath(path) + "/settings/study";
    }

    @PostMapping("/recruit/stop")
    public String stopRecruit(@CurrentAccount Account account, @PathVariable String path, Model model
            , RedirectAttributes attributes) {
        Study studyToUpdateStatus = studyService.getStudyToUpdateStatus(account, path);

        if (!studyToUpdateStatus.canUpdateRecruiting()) {
            attributes.addFlashAttribute("message", "1시간 안에 인원 모집 설정을 여러 번 변경할 수 없습니다.");
            return "redirect:/study/" + getPath(path) + "/settings/study";
        }
        studyService.stopRecruit(studyToUpdateStatus);
        attributes.addFlashAttribute("message", "인원 모집을 종료합니다.");
        return "redirect:/study/" + getPath(path) + "/settings/study";
    }


}
