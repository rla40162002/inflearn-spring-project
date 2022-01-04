package com.studylecture.study;

import com.studylecture.domain.Account;
import com.studylecture.domain.Study;
import com.studylecture.study.form.StudyDescriptionForm;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


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
        if (byPath == null) {
            throw new IllegalArgumentException(path + "에 해당하는 스터디가 없습니다.");
        }
        return byPath;
    } // getStudy


    public Study getStudyToUpdate(Account account, String path) {
        Study study = getStudy(path);

        if (!account.isManagerOf(study)) { // 관리자가 아니면
            throw new AccessDeniedException("해당 기능을 사용할 권한이 없습니다.");
        }

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

}
