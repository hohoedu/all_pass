
import com.hohoedu.all_pass.payment.Payment;
import com.hohoedu.all_pass.payment.model.PaymentBill;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;

@Entity
@Getter
@Table(name = "erp_payment_callback")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentCallback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "bill_id", nullable = false, length = 50)
    private String billId; // 결제선생 bill_id (외부 청구서 ID)

    @Column(name = "appr_state", nullable = false)
    private String apprState; // 결제 상태 (1: 승인완료, 9: 취소)

    @Column(name = "appr_date")
    private String apprDate; // 승인 일자 (yyyy-MM-dd HH:mm)

    @Column(name = "appr_price")
    private String apprPrice; // 결제 금액

    @Column(name = "appr_pay_type")
    private String apprPayType; // 결제 수단 (card, bank 등)

    @Column(name = "appr_card_type")
    private String apprCardType; // 카드 종류 (VISA, BC 등)

    @Column(name = "appr_issuer")
    private String apprIssuer; // 카드사

    @Column(name = "appr_num")
    private String apprNum; // 승인번호

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bill_id", referencedColumnName = "bill_id", insertable = false, updatable = false)
    private PaymentBill paymentBill;

    @CreationTimestamp
    private Timestamp createdAt;

    @Builder
    public PaymentCallback(String billId, String apprState, String apprDate, String apprPrice,
                           String apprPayType, String apprCardType, String apprIssuer, String apprNum) {
        this.billId = billId;
        this.apprState = apprState;
        this.apprDate = apprDate;
        this.apprPrice = apprPrice;
        this.apprPayType = apprPayType;
        this.apprCardType = apprCardType;
        this.apprIssuer = apprIssuer;
        this.apprNum = apprNum;
    }
}