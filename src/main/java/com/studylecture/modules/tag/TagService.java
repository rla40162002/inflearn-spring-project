package com.studylecture.modules.tag;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;

    public Tag findOrCreateNew(String tagTitle) {
        Tag tag = tagRepository.findByTitle(tagTitle);
        // Optional 로 바꾼 후
        // Tag tag =   tagRepository.findByTitle(title).orElseGet(() -> tagRepository.save(Tag.builder()
        //                      .title(tagForm.getTagTitle())
        //                      .build())); // findByTitle 쪽도 return 값을 Optional 로 바꿔야함.
        if (tag == null) {
            tag = tagRepository.save(Tag.builder().title(tagTitle).build()); // tagForm
        }

        return tag;
    }
}
