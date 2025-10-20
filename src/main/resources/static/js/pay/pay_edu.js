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


// ========== 우측 버튼 클릭 ========== //
document.addEventListener("DOMContentLoaded", () => {
    const payIssue = document.querySelector('#pay-issue');
    const payCancel = document.querySelector('#pay-cancel');
    const payDestroy = document.querySelector('#pay-destory');
    const payReissue = document.querySelector('#pay-reissue')
    if (!payIssue) return;

// 결제선생 테스트 URL
    payIssue.addEventListener('click', () => {
        const bill = {
            bill_id: "32088000280123456790",
            product_nm: "청구사유",
            message: "안내메시지",
            member_nm: "박세환",
            phone: "01062954886",
            price: "100000",
            hash: "9d2f756b6d7028a83bbeb588f44e6b42a1305d03c10adb209dc0968ad172fcd2",
            expire_dt: "2025-10-20",
            callbackURL: "https://hohocenter.co.kr/pay/callback"
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
            })
            .catch(err => {
                console.error("오류 발생:", err);
            });
    });

    payCancel.addEventListener('click', () => {
        console.log('취소 버튼');
    });
    payDestroy.addEventListener('click', () => {
        console.log('파기 버튼');
    });
    payReissue.addEventListener('click', () => {
        console.log('재발행 버튼 ');
    });


});