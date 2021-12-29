package com.studylecture.account;

import com.studylecture.domain.Account;
import com.studylecture.domain.Tag;
import com.studylecture.settings.form.Notifications;
import com.studylecture.settings.form.Profile;
import com.studylecture.account.form.SignUpForm;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
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
import java.util.Optional;

@Transactional
@Service
@RequiredArgsConstructor
public class AccountService implements UserDetailsService {
    private final AccountRepository accountRepository;
    private final JavaMailSender javaMailSender;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;

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
    } // login

    @Transactional(readOnly = true)
    @Override
    public UserDetails loadUserByUsername(String emailOrNickname) throws UsernameNotFoundException {
        Account account = accountRepository.findByEmail(emailOrNickname); // 이메일 먼저 던져보고
        if (account == null) { // null 인 경우엔 닉네임 조회
            account = accountRepository.findByNickname(emailOrNickname);
        }

        if (account == null) {
            throw new UsernameNotFoundException(emailOrNickname);
        }
        return new UserAccount(account); // principal
    } // loadUserByUsername

    public void completeSignup(Account account) {
        account.completeSignUp(); // verified true, joinedAt now
        login(account);
    } // completeSignup

    public void updateProfile(Account account, Profile profile) {
        modelMapper.map(profile, account); // profile을 account에 저장한다 라는 의미
        accountRepository.save(account);
    } // updateProfile

    public void updatePassword(Account account, String newPassword) {
        account.setPassword(passwordEncoder.encode(newPassword)); // 현재 객체는 detached 상태의 객체다. persist x
        accountRepository.save(account); // 명시적으로 merge 해줘야 함.
    } // updatePassword

    public void updateNotifications(Account account, Notifications notifications) {
        modelMapper.map(notifications, account);
        accountRepository.save(account);
    } // updateNotifications

    public void updateNickname(Account account, String nickname) {
        account.setNickname(nickname);
        accountRepository.save(account);
        login(account); // 이걸 안하면 네비게이션바에 있는 정보가 바뀌지 않는다.
    } // updateNickname

    public void sendLoginLink(Account account) { // 이메일로 로그인하는 메일 발송
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        System.out.println("check sendLoginLink method");
        account.generateEmailCheckToken(); // 이메일로그인 토큰 새로 생성
        mailMessage.setTo(account.getEmail()); // 받는 사람
        mailMessage.setSubject("스터디 사이트, 로그인 링크"); // 제목
        mailMessage.setText("/login-by-email?token=" + account.getEmailCheckToken()
                + "&email=" + account.getEmail()); // 내용
        javaMailSender.send(mailMessage);
    } // sendLoginLink

    public void addTag(Account account, Tag tag) {
        Optional<Account> byId = accountRepository.findById(account.getId());
        byId.ifPresent(a -> a.getTags().add(tag));
    } // addTag
}
