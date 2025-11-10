package com.hohoedu.all_pass.payment;

import com.hohoedu.all_pass._core.utils.PaymentKeyGenerator;
import com.hohoedu.all_pass.center.Center;
import com.hohoedu.all_pass.student.Student;
import com.hohoedu.all_pass.user.User;
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
@Table(name = "erp_payment", uniqueConstraints = @UniqueConstraint(name = "uq_payment_key", columnNames = "payment_key"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "payment_key", nullable = false, unique = true, length = 30)
    private String paymentKey; // 결제 고유 식별자
    //    - 청구서와 무관하게 모든 결제에 유일하게 부여됨

    @Column(name = "bill_id")
    private String billId; // 결제선생에서 사용하는 청구서 ID
    //    - 결제선생 결제 시에는 실제 bill_id 저장
    //    - 수기/선결제 등 오프라인 결제는 null 가능

    @Column(name = "product_name")
    private String productName; // 청구 항목명 (예: 교육비, 교재비 등)

    @Column(length = 20)
    private String method; // 결제 수단 (예: online_card, offline_card, cash, bank_transfer 등)

    @Column(length = 20, nullable = false)
    private String source; // 결제 발생 소스 구분
    //    - "system" : 자동 생성 (결제선생 청구서 발행)
    //    - "manual" : 수기 입력
    //    - "preset" : 선결제 등록
    //    - "callback" : 결제선생 콜백

    @Column(nullable = false)
    private String amount; // 결제 금액
    //    - 수기 입력 또는 콜백 금액 등 결제 실제 금액

    @Column(length = 20, nullable = false)
    private String status; // 결제 상태
    //    - issued : 청구서 발행 (결제 대기)
    //    - approved : 결제 완료
    //    - cancelled : 결제 취소
    //    - expired : 청구 만료
    //    - linked : 선결제 연결 완료

    @Column(name = "paid_date")
    private String paidDate; // 결제가 완료된 날짜 (yyyy-MM-dd HH:mm 형식)

    @Column(name = "request_date")
    private String requestDate; // 청구서가 생성된 날짜 (yyyy-MM-dd)

    @Column
    private String yy;

    @Column
    private String mm;

    @Column(columnDefinition = "TEXT")
    private String note;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", referencedColumnName = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "center_code", referencedColumnName = "center_code", nullable = false)
    private Center center;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_code", referencedColumnName = "user_code")
    private User user;

    @CreationTimestamp
    private Timestamp createdAt;

    @UpdateTimestamp
    private Timestamp updatedAt;

    @PrePersist
    public void generatePaymentKey() {
        if (this.paymentKey == null && this.center != null) {
            this.paymentKey = PaymentKeyGenerator.generate(center.getCenterCode());
        }
    }

    @Builder
    public Payment(String billId, Student student, Center center, User user, String productName,
                   String method, String source, String amount, String status,
                   String paidDate, String requestDate, String yy, String mm, String note) {
        this.billId = billId;
        this.student = student;
        this.center = center;
        this.user = user;
        this.productName = productName;
        this.method = method;
        this.source = source;
        this.amount = amount;
        this.status = status;
        this.paidDate = paidDate;
        this.requestDate = requestDate;
        this.yy = yy;
        this.mm = mm;
        this.note = note;
    }
}