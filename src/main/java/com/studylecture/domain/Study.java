package com.studylecture.domain;

import lombok.*;

import javax.persistence.*;
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
        this.managers.add(account);
    } // addManager

}
