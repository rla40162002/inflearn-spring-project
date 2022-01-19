package com.studylecture.modules.study;

import com.studylecture.modules.account.Account;
import com.studylecture.modules.account.UserAccount;
import com.studylecture.modules.tag.Tag;
import com.studylecture.modules.zone.Zone;
import lombok.*;

import javax.persistence.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@Builder
public class Study {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToMany
    private Set<Account> managers = new HashSet<>(); // 관리자 (여려 명)

    @ManyToMany
    private Set<Account> members = new HashSet<>();

    @Column(unique = true)
    private String path;

    private String title;

    private String shortDescription;

    @Lob
    @Basic(fetch = FetchType.EAGER)
    private String fullDescription;

    @Lob
    @Basic(fetch = FetchType.EAGER)
    private String image;

    @ManyToMany
    private Set<Tag> tags = new HashSet<>();

    @ManyToMany
    private Set<Zone> zones = new HashSet<>();

    private LocalDateTime publishedDateTime; // 스터디를 공개한 시간

    private LocalDateTime closedDateTime; // 스터디를 종료한 시간

    private LocalDateTime recruitingUpdateDateTime; // 인원모집했던 시간

    private boolean recruiting; // 모집중 여부

    private boolean published; // 공개 여부

    private boolean closed; // 종료 여부

    private boolean useBanner; // 배너 사용 여부

    public void addManager(Account account) {
        this.getManagers().add(account);
    } // addManager

    public void addMember(Account account) {
        this.getMembers().add(account);
    } // addMember

    public void removeMember(Account account) {
        this.getMembers().remove(account);
    }


    public boolean isJoinable(UserAccount userAccount) {
        Account account = userAccount.getAccount();
        return this.isPublished() && this.isRecruiting() && !this.members.contains(account) && !this.managers.contains(account);
    }

    public boolean isMember(UserAccount userAccount) {
        return this.members.contains(userAccount.getAccount());
    }

    public boolean isManager(UserAccount userAccount) {
        return this.managers.contains(userAccount.getAccount());
    }

    public String getImage() {
        return image != null ? image : "/images/default_banner.png";
    }

    public void publish() {
        if (!this.closed && !this.published) { // 스터디가 공개되지 않은 상태이고 종료가 되지 않은 상태
            this.published = true;
            this.publishedDateTime = LocalDateTime.now();
        } else {
            throw new RuntimeException("스터디를 공개할 수 없는 상태입니다. 스터디를 이미 공개했거나 종료했습니다.");
        }
    } // publish

    public void close() {
        if (!this.closed && this.published) { // 스터디가 공개된 상태이고, 종료되지 않은 상태
            this.closed = true;
            this.closedDateTime = LocalDateTime.now();
        } else {
            throw new RuntimeException("스터디를 종료할 수 없습니다. 스터디를 공개하지 않았거나 이미 종료한 스터디입니다.");
        }
    } // close

    public void startRecruit() {
        if (canUpdateRecruiting()) { // 스터디가 공개된 상태이고, 종료되지 않은 상태
            this.recruiting = true;
            this.recruitingUpdateDateTime = LocalDateTime.now();
        } else {
            throw new RuntimeException("인원 모집을 시작할 수 없습니다. 스터디를 공개하거나 한 시간 뒤 다시 시도하세요.");
        }
    } // startRecruit

    public void stopRecruit() {
        if (canUpdateRecruiting()) { // 스터디가 공개된 상태이고, 종료되지 않은 상태
            this.recruiting = false;
            this.recruitingUpdateDateTime = LocalDateTime.now();
        } else {
            throw new RuntimeException("인원 모집을 중단할 수 없습니다. 스터디를 공개하거나 한 시간 뒤 다시 시도하세요.");
        }
    } // stopRecruit


    public boolean canUpdateRecruiting() {
        return this.published && this.recruitingUpdateDateTime == null
                || this.recruitingUpdateDateTime.isBefore(LocalDateTime.now().minusHours(1));
    } // canUpdateRecruiting

    public String getEncodePath() {
        return URLEncoder.encode(this.path, StandardCharsets.UTF_8);
    } // getEncodePath

    public boolean isRemovable() {
        return !this.published; // TODO : 모임을 했던 스터디는 삭제 못하는 조건 추가
    } // isRemovable


    public boolean isManagedBy(Account account) {
        return this.getManagers().contains(account);
    }
}
