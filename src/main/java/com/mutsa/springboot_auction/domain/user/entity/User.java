package com.mutsa.springboot_auction.domain.user.entity;

import com.mutsa.springboot_auction.global.common.BaseTimeEntity;
import jakarta.persistence.*;

import java.util.Collection;
import java.util.Collections;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Table(name = "app_users")
public class User extends BaseTimeEntity implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "socialId", nullable = false, unique = true)
    private String socialId;

    @Column(name = "nickname", unique = true)
    private String nickname;

    private String profileImageUrl;

    @Column(nullable = false)
    @Builder.Default
    private Integer point = 0;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Enumerated(EnumType.STRING)
    private SocialType socialType;

    public User updateProfileImg(String imgUrl) {
        this.profileImageUrl = imgUrl;
        return this;
    }

    public void chargePoint(Integer amount) {
        this.point += amount;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority(role.name()));
    }

    public void addPoint(Integer amount) {
        this.point += amount;
    }

    public void subtractPoint(Integer amount) {
        this.point -= amount;
    }

    @Override
    public String getPassword() {
        return "";
    }

    @Override
    public String getUsername() {
        return "";
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }
}
