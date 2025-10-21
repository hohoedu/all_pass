document.addEventListener("DOMContentLoaded", () => {

    function openModal(modalType) {
        console.log('modalType=' + modalType);
        document.querySelectorAll('.modal').forEach(modal => {
            modal.style.display = 'none';
        });

        const targetModal = document.querySelector(`.${modalType}-modal`);
        console.log("targetModal" + targetModal);
        if (targetModal) {
            targetModal.style.display = 'block';
        }
    }

    const btnTuition = document.querySelector('#btn-tuition');
    if (btnTuition) {
        btnTuition.addEventListener('click', () => {
            console.log('버튼 클릭!!')
            openModal('tuition');
        });
    }

    document.querySelectorAll('#student-tbody tr').forEach(row => {
        row.addEventListener('click', () => {
            openModal('personal');
        });
    });
});

const bill_id = "32088000282510211340";
const phone = "01062954886";
const price = "50000";
sendHash = "";
cancelHash = "";

async function generateSendHash(billId, phone, price) {
    const encoder = new TextEncoder();
    const data = encoder.encode(`${billId},${phone},${price}`);
    const hashBuffer = await crypto.subtle.digest('SHA-256', data);
    const hashArray = Array.from(new Uint8Array(hashBuffer));
    const sendHash = hashArray.map(b => b.toString(16).padStart(2, '0')).join('');
    return sendHash;
}

async function generateCancelHash(billId, price) {
    const encoder = new TextEncoder();
    const data = encoder.encode(`${billId},${price}`);
    const hashBuffer = await crypto.subtle.digest('SHA-256', data);
    const hashArray = Array.from(new Uint8Array(hashBuffer));
    const cancelHash = hashArray.map(b => b.toString(16).padStart(2, '0')).join('');
    return cancelHash;
}

(async () => {
    sendHash = await generateSendHash(bill_id, phone, price);
    cancelHash = await generateCancelHash(bill_id, price);

    console.log("✅ sendHash:", sendHash);
    console.log("✅ cancelHash:", cancelHash);
})();
// ========== 우측 버튼 클릭 ========== //
document.addEventListener("DOMContentLoaded", () => {
    const payIssue = document.querySelector('#pay-issue');
    const payCancel = document.querySelector('#pay-cancel');
    const payDestroy = document.querySelector('#pay-destory');
    const payReissue = document.querySelector('#pay-reissue')
    if (!payIssue) return;


    // 결제선생 테스트
    payIssue.addEventListener('click', () => {
        const bill = {
            // bill_id: "DAE00125102112540000",
            bill_id: bill_id,
            product_nm: "청구사유",
            message: "안내메시지",
            member_nm: "박세환",
            phone: phone,
            price: price,
            hash: sendHash,
            expire_dt: "2025-10-27",
            callbackURL: "https://6d5da39844c8.ngrok-free.app/pay/callback"
        }
        const requestBody = {
            apikey: "TEST-API-KEY-TALK",
            member: "TEST-MEMBER-FOR-API",
            merchant: "TEST-MERCHANT-FOR-API",
            bill: bill
        };

        fetch("http://stg.paymint.co.kr:10200/if/bill/send", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Accept": "application/json"
            },
            body: JSON.stringify(requestBody)
        })
            .then(res => {
                console.log("응답 상태:", res.status);
                return res.json(); // JSON 파싱
            })
            .then(data => {
                console.log("요청 바디:", JSON.stringify(requestBody));
                console.log("응답 데이터:", data);
                if (data.code === "9800") {
                    alert('이미 발행된 청구서 입니다.')
                }
            })
            .catch(err => {
                console.error("오류 발생:", err);
            });
    });

    payCancel.addEventListener('click', () => {
        console.log('취소 버튼');


        const requestBody = {
            apikey: "TEST-API-KEY-TALK",
            member: "TEST-MEMBER-FOR-API",
            merchant: "TEST-MERCHANT-FOR-API",
            bill_id: bill_id,
            price: price,
            hash: cancelHash
        };

        fetch("http://stg.paymint.co.kr:10200/if/bill/cancel", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Accept": "application/json"
            },
            body: JSON.stringify(requestBody)
        })
            .then(res => {
                console.log("응답 상태:", res.status);
                return res.json(); // JSON 파싱
            })
            .then(data => {
                console.log("요청 바디:", JSON.stringify(requestBody));
                console.log("응답 데이터:", data);
                console.log(data.code);
                if (data.code === "0000") {
                    alert('결제가 취소되었습니다.');
                    return;
                }
                if (data.code === "9980") {
                    alert('청구서를 찾을 수 없습니다.');

                }
            })
            .catch(err => {
                console.error("오류 발생:", err);
            });
    });
    payDestroy.addEventListener('click', () => {
        console.log('파기 버튼');

        const requestBody = {
            apikey: "TEST-API-KEY-TALK",
            member: "TEST-MEMBER-FOR-API",
            merchant: "TEST-MERCHANT-FOR-API",
            bill_id: bill_id,
            price: price,
            hash: cancelHash
        };

        fetch("http://stg.paymint.co.kr:10200/if/bill/cancel", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Accept": "application/json"
            },
            body: JSON.stringify(requestBody)
        })
            .then(res => {
                console.log("응답 상태:", res.status);
                return res.json(); // JSON 파싱
            })
            .then(data => {
                console.log("요청 바디:", JSON.stringify(requestBody));
                console.log("응답 데이터:", data);
                console.log(data.code);
                if (data.code === "0000") {
                    alert('결제가 취소되었습니다.');
                    return;
                }
                if (data.code === "9980") {
                    alert('청구서를 찾을 수 없습니다.');

                }
            })
            .catch(err => {
                console.error("오류 발생:", err);
            });
    });
    payReissue.addEventListener('click', () => {
        console.log('재발행 버튼 ');
    });


});