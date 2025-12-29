package com.hohoedu.all_pass.payment.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "erp_card_code")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CardCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "card_code", unique = true)
    private String cardCode;

    @Column(name = "card_name")
    private String cardName;

    @Column(name = "use_yn")
    private boolean useYn;

    @Builder
    public CardCode(String cardCode, String cardName, boolean useYn) {
        this.cardCode = cardCode;
        this.cardName = cardName;
        this.useYn = useYn;
    }
}
