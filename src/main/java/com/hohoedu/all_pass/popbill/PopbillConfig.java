package com.hohoedu.all_pass.popbill;

import com.hohoedu.all_pass.center.Center;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.sql.Timestamp;

@Entity
@Getter
@Table(name = "erp_popbill_config")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PopbillConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "corp_number", nullable = false)
    private String corpNumber;

    @Column(name = "link_id", nullable = false)
    private String linkId;

    @Column(name = "popbill_id", nullable = false)
    private String popbillId;

    @Column(name = "encrypted_key", nullable = false)
    private String encryptedKey;

    @Column(name = "sender_number", nullable = false)
    private String senderNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "center_code", nullable = false, referencedColumnName = "center_code")
    private Center center;

    @Column(name = "created_at")
    @CreationTimestamp
    private Timestamp createdAt;

    @Column(name = "updatedAt")
    @UpdateTimestamp
    private Timestamp updatedAt;

    @Builder
    public PopbillConfig(String corpNumber, String linkId, String popbillId, String encryptedKey, String senderNumber, Center center) {
        this.corpNumber = corpNumber;
        this.linkId = linkId;
        this.popbillId = popbillId;
        this.encryptedKey = encryptedKey;
        this.senderNumber = senderNumber;
        this.center = center;
    }
}
