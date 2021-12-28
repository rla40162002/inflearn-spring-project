package com.studylecture.domain;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Entity
@Getter
@Setter
@EqualsAndHashCode(of = "id")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Account {

    @Id
    @GeneratedValue
    private Long id;

    @Column(unique = true)
    private String email;

    @Column(unique = true)
    private String nickname;
    private String password;
    private boolean emailVerified; // 이메일 인증이 됐는지 확인
    private String emailCheckToken; // 이메일 검증할때 사용할 토큰 값 db에 저장해놓고 매치하는지 확인
    private LocalDateTime emailCheckTokenGeneratedAt; // 이메일 토큰 만들어진 시간
    private LocalDateTime joinedAt; // 가입날짜(인증을 거친 사용자들), 인증이 완료되는 순간 기준
    private String bio; // 자기소개
    private String url; // 웹사이트 url
    private String occupation; // 직업
    private String location; // 거주지

    @Lob
    @Basic(fetch = FetchType.EAGER)
    private String profileImage; // 프로필 이미지 저장

    private boolean studyCreatedByEmail; // 스터디 만들어진 거 이메일로 받기 여부

    private boolean studyCreatedByWeb = true; // 웹으로 받기 여부

    private boolean studyJoinResultByEmail; // 스터디 가입신청 결과 이메일로 받기 여부
    private boolean studyJoinResultByWeb = true; // 웹으로 받기 여부

    private boolean studyUpdatedByEmail; // 스터디 갱신 이메일 여부
    private boolean studyUpdatedByWeb = true; // 스터디 갱신 웹 여부

    @ManyToMany
    private Set<Tag> tags;


    public void generateEmailCheckToken() {
        this.emailCheckToken = UUID.randomUUID().toString(); // 랜덤값 만들어서 저장
        this.emailCheckTokenGeneratedAt = LocalDateTime.now();
    }

    public void completeSignUp() {
        setEmailVerified(true);
        setJoinedAt(LocalDateTime.now());
    }

    public boolean isValidToken(String token) {
        return this.emailCheckToken.equals(token);
    }

    public boolean canSendConfirmEmail() {
        return this.emailCheckTokenGeneratedAt.isBefore(LocalDateTime.now().minusHours(1));
        // 1시간이 안 지났으면 false
//        return true;
    }
}
