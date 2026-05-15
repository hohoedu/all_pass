package com.hohoedu.all_pass.student.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;


@Entity
@Getter
@Table(name = "erp_invite_tracking")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InviteTracking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "send_key")
    private String sendKey;

    @Column(name = "invite_code")
    private String inviteCode;

    @Column(name = "user_code")
    private String userCode;

    @Column(name = "receiver_phone")
    private String receiverPhone;

    @Column(name = "center_code")
    private String centerCode;

    @Column(name = "invite_status")
    private String inviteStatus;

    @Column(name = "completed_at")
    private Timestamp completedAt;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Builder
    public InviteTracking(String sendKey, String inviteCode, String userCode, String receiverPhone, String centerCode, String inviteStatus) {
        this.sendKey = sendKey;
        this.inviteCode = inviteCode;
        this.userCode = userCode;
        this.receiverPhone = receiverPhone;
        this.centerCode = centerCode;
        this.inviteStatus = inviteStatus;
    }
}
