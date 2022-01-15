package com.studylecture.study;

import com.studylecture.domain.Account;
import com.studylecture.domain.Study;
import com.studylecture.domain.Tag;
import com.studylecture.domain.Zone;
import com.studylecture.study.form.StudyDescriptionForm;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.studylecture.study.form.StudyForm.VALID_PATH_PATTERN;


@Service
@Transactional
@RequiredArgsConstructor
public class StudyService {
    private final StudyRepository studyRepository;
    private final ModelMapper modelMapper;

    public Study createNewStudy(Study study, Account account) {
        Study newStudy = studyRepository.save(study);
        newStudy.addManager(account);
        return newStudy;
    } // createNewStudy

    public Study getStudy(String path) {
        Study byPath = studyRepository.findByPath(path);
        checkIfExistingStudy(path, byPath);
        return byPath;
    } // getStudy


    public Study getStudyToUpdate(Account account, String path) {
        Study study = getStudy(path);
        checkIfManager(account, study);
        return study;
    }

    public void updateStudyDescription(Study study, StudyDescriptionForm studyDescriptionForm) {
        modelMapper.map(studyDescriptionForm, study);
    } // updateStudyDescription

    public void updateStudyImage(Study study, String image) {
        study.setImage(image);
    } // updateStudyImage

    public void enableStudyBanner(Study study) {
        study.setUseBanner(true);
    } // enableStudyBanner

    public void disableStudyBanner(Study study) {
        study.setUseBanner(false);
    } // disableStudyBanner

    public void addTag(Study study, Tag tag) {
        study.getTags().add(tag);
    } // addTag

    public void removeTag(Study study, Tag tag) {
        study.getTags().remove(tag);
    } // removeTag

    public void addZone(Study study, Zone zone) {
        study.getZones().add(zone);
    } // addZone

    public void removeZone(Study study, Zone zone) {
        study.getZones().remove(zone);
    } // removeZone

    public Study getStudyToUpdateTag(Account account, String path) {
        Study study = studyRepository.findStudyWithTagsByPath(path);
        checkIfExistingStudy(path, study);
        checkIfManager(account, study);
        return study;
    } // getStudyToUpdateTag

    public Study getStudyToUpdateZone(Account account, String path) {
        Study study = studyRepository.findStudyWithZonesByPath(path);
        checkIfExistingStudy(path, study);
        checkIfManager(account, study);
        return study;
    } // getStudyToUpdateZone

    public Study getStudyToUpdateStatus(Account account, String path) {
        Study study = studyRepository.findStudyWithManagersByPath(path);
        checkIfExistingStudy(path, study);
        checkIfManager(account, study);
        return study;
    } // getStudyToUpdateStatus


    private void checkIfManager(Account account, Study study) {
        if (!account.isManagerOf(study)) {
            throw new AccessDeniedException("해당 기능을 사용할 권한이 없습니다.");
        }
    }

    private void checkIfExistingStudy(String path, Study study) {
        if (study == null) {
            throw new IllegalArgumentException(path + "에 해당하는 스터디가 없습니다.");
        }
    }

    public void publish(Study study) {
        study.publish();
    }

    public void close(Study study) {
        study.close();
    }

    public void startRecruit(Study study) {
        study.startRecruit();
    }

    public void stopRecruit(Study study) {
        study.stopRecruit();
    }

    public boolean isValidPath(String newPath) {
        if (!newPath.matches(VALID_PATH_PATTERN)) {
            return false;
        }
        return !studyRepository.existsByPath(newPath);
    } // isValidPath

    public void updateStudyPath(Study study, String newPath) {
        study.setPath(newPath);
    } // updateStudyPath

    public boolean isValidTitle(String newTitle) {
        return newTitle.length() <= 50;
    } // isValidTitle

    public void updateStudyTitle(Study study, String newTitle) {
        study.setTitle(newTitle);
    } // updateStudyTitle

    public void removeStudy(Study study) {
        if (study.isRemovable()) {
            studyRepository.delete(study);
        } else {
            throw new IllegalArgumentException("스터디를 삭제할 수 없습니다.");
        }
    } // remove

    public void addMember(Study study, Account account) {
        study.addMember(account);
    } // addMember

    public void removeMember(Study study, Account account) {
        study.removeMember(account);
    } // removeMember

    public Study getStudyToEnroll(String path) {
        Study study = studyRepository.findStudyOnlyByPath(path);
        checkIfExistingStudy(path, study);
        return study;
    } // getStudyToEnroll
}
