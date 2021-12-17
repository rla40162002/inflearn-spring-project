package com.studylecture.account;

import com.studylecture.domain.Account;
import com.studylecture.settings.Profile;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
@Service
@RequiredArgsConstructor
public class AccountService implements UserDetailsService {
    private final AccountRepository accountRepository;
    private final JavaMailSender javaMailSender;
    private final PasswordEncoder passwordEncoder;

    public Account processNewAccount(SignUpForm signUpForm) { // 새로운 계정 처리 부분
        Account newAccount = saveNewAccount(signUpForm);
        // saveNewAccount 나온 후엔 해당 안됨
        // persist 상태 객체는 종료될때 db에 싱크를 하게 됨. Transactional
        // 토큰 만들어서 메시지에 담고 보내는 부분
        newAccount.generateEmailCheckToken(); // 저장
        sendSignUpConfirmEmail(newAccount);

        return newAccount;
    } // processNewAccount


    private Account saveNewAccount(SignUpForm signUpForm) { // 계정 저장하는 부분
        Account account = Account.builder()
                .email(signUpForm.getEmail())
                .nickname(signUpForm.getNickname())
                .password(passwordEncoder.encode(signUpForm.getPassword()))   //TODO encoding 해야함
                .studyCreatedByWeb(true)
                .studyUpdatedByWeb(true)
                .studyJoinResultByWeb(true) // web 알림만 켜두기
                .build();

        Account newAccount = accountRepository.save(account);
        return newAccount;
    } // saveNewAccount

    public void sendSignUpConfirmEmail(Account newAccount) { // 인증 메일 보내는 부분
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(newAccount.getEmail()); // 받는 사람
        mailMessage.setSubject("스터디 사이트 연습, 회원 가입 인증"); // 제목
        mailMessage.setText("/check-email-token?token=" + newAccount.getEmailCheckToken()
                + "&email=" + newAccount.getEmail()); // 내용
        javaMailSender.send(mailMessage);
    } // sendSignUpConfirmEmail

    public void login(Account account) {
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                new UserAccount(account),
                account.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_USER")));

//        UsernamePasswordAuthenticationToken token2 = new UsernamePasswordAuthenticationToken(account.getNickname(), account.getPassword());
//        AuthenticationManager authenticationManager = authenticationManager.authenticate(token);
// 원래는 이런식으로 주입받아서 써야하지만

        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(token);
    }

    @Transactional(readOnly = true)
    @Override
    public UserDetails loadUserByUsername(String emailOrNickname) throws UsernameNotFoundException {
        Account account = accountRepository.findByEmail(emailOrNickname); // 이메일 먼저 던져보고
        if (account == null) { // null인 경우엔 닉네임 조회
            account = accountRepository.findByNickname(emailOrNickname);
        }

        if (account == null) {
            throw new UsernameNotFoundException(emailOrNickname);
        }
        return new UserAccount(account); // principle
    }

    public void completeSignup(Account account) {
        account.completeSignUp(); // verified true, joinedAt now
        login(account);
    }

    public void updateProfile(Account account, Profile profile) {
        account.setUrl(profile.getUrl());
        account.setOccupation(profile.getOccupation());
        account.setLocation(profile.getLocation());
        account.setBio(profile.getBio());

        // TODO 프로필 이미지
        accountRepository.save(account);
        // TODO 문제 하나 더
    }
}
