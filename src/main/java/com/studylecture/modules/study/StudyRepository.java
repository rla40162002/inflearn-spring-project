package com.studylecture.modules.study;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface StudyRepository extends JpaRepository<Study, Long> {
    boolean existsByPath(String path);

    @EntityGraph(attributePaths = {"tags", "zones", "managers", "members"}, type = EntityGraph.EntityGraphType.LOAD)
    Study findByPath(String path);

    @EntityGraph(attributePaths = {"tags", "managers"}, type = EntityGraph.EntityGraphType.FETCH)
    Study findStudyWithTagsByPath(String path);

    @EntityGraph(attributePaths = {"zones", "managers"}, type = EntityGraph.EntityGraphType.FETCH)
    Study findStudyWithZonesByPath(String path);

    @EntityGraph(attributePaths = {"managers"}, type = EntityGraph.EntityGraphType.FETCH)
    Study findStudyWithManagersByPath(String path);

    @EntityGraph(attributePaths = {"members"}, type = EntityGraph.EntityGraphType.FETCH)
    Study findStudyWithMembersByPath(String path);

    Study findStudyOnlyByPath(String path);

    @EntityGraph(attributePaths = {"tags", "zones"}, type = EntityGraph.EntityGraphType.FETCH)
    Study findStudyWithTagsAndZonesById(Long id);

    @EntityGraph(attributePaths = {"managers", "members"})
    Study findStudyWithManagersAndMembersById(Long id);

}
