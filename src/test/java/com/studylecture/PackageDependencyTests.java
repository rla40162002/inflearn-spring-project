package com.studylecture;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

@AnalyzeClasses(packagesOf = StudyLectureApplication.class)
public class PackageDependencyTests {

    private static final String STUDY = "..modules.study..";
    private static final String EVENT = "..modules.event..";
    private static final String ACCOUNT = "..modules.account..";
    private static final String TAG = "..modules.tag..";
    private static final String ZONE = "..modules.zone..";
    private static final String MAIN = "..modules.main..";

    @ArchTest
    ArchRule modulesPackageRule = classes().that().resideInAPackage("com.studylecture.modules..")
            .should().onlyBeAccessed().byClassesThat()
            .resideInAnyPackage("com.studylecture.modules..");


    @ArchTest
    ArchRule studyPackageRule = classes().that().resideInAPackage(STUDY)
            .should().onlyBeAccessed().byClassesThat()
            .resideInAnyPackage(STUDY, EVENT, MAIN); // 스터디와 이벤트에서만 접근이 가능해야 한다.

    @ArchTest
    ArchRule eventPackageRule = classes().that().resideInAPackage(EVENT)
            .should().accessClassesThat().resideInAnyPackage(STUDY, ACCOUNT, EVENT);
    // 이벤트에 들어있는 것들은 스터디, 어카운트, 이벤트를 참조한다.

    @ArchTest
    ArchRule accountPackageRule = classes().that().resideInAPackage(ACCOUNT)
            .should().accessClassesThat().resideInAnyPackage(TAG, ZONE, ACCOUNT);
    // account 에 있는 것은 TAG, ZONE ACCOUNT를 참조한다.

    @ArchTest
    ArchRule cycleCheck = slices().matching("com.studylecture.modules.(*)..") // 해당 위치 패키지들을 슬라이스
            .should().beFreeOfCycles(); // 슬라이스 간 순환참조가 있는지 확인




}
