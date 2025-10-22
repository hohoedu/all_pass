
document.addEventListener('DOMContentLoaded', () => {
    const monthInput = document.querySelector('.hidden-picker');
    const monthBtn = document.querySelector('.calendar-open');
    const monthDisplay = document.querySelector('.day-display');

    const now = new Date();
    const currentYear = now.getFullYear();
    const currentMonth = now.getMonth() + 1;

    // 기본값: 현재 월 표시
    monthInput.value = `${currentYear}-${String(currentMonth).padStart(2, '0')}-01`;
    monthDisplay.insertAdjacentText('afterbegin', `${currentYear}년 ${currentMonth}월`);

    // 달력 아이콘 클릭 시 열기
    monthBtn.addEventListener('click', () => {
        monthInput.showPicker();
    });

    // 선택 후 표시 업데이트
    monthInput.addEventListener('change', () => {
        const date = new Date(monthInput.value);
        if (isNaN(date)) return;
        const year = date.getFullYear();
        const month = date.getMonth() + 1;
        monthDisplay.childNodes[0].textContent = `${year}년 ${month}월`;
    });
});

document.addEventListener('DOMContentLoaded', () => {
    /* =======================
       2️⃣ 일 달력 (만료일용)
    ======================= */
    const expireInput = document.querySelector('.expire-input');
    const expireBtn = document.querySelector('.expire-btn');
    const expireDisplay = document.querySelector('.day-picker .day-display');

    // 기본값: 오늘 + 5일
    const plus5 = new Date();
    plus5.setDate(now.getDate() + 5);

    const y = plus5.getFullYear();
    const m = plus5.getMonth() + 1;
    const d = plus5.getDate();

    expireInput.value = `${y}-${String(m).padStart(2, '0')}-${String(d).padStart(2, '0')}`;
    expireDisplay.textContent = `${y}년 ${m}월 ${d}일`;

    const today = new Date();
    const minY = today.getFullYear();
    const minM = today.getMonth() + 1;
    const minD = today.getDate();
    expireInput.min = `${minY}-${String(minM).padStart(2, '0')}-${String(minD).padStart(2, '0')}`;

    // 아이콘 클릭 시 달력 열기
    expireBtn.addEventListener('click', () => {
        expireInput.showPicker();
    });

    // 날짜 선택 시 표시 업데이트
    expireInput.addEventListener('change', () => {
        const date = new Date(expireInput.value);
        if (isNaN(date)) return;
        expireDisplay.textContent = `${date.getFullYear()}년 ${date.getMonth() + 1}월 ${date.getDate()}일`;
    });
});
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
        row.addEventListener('mouseenter', () => {
            row.style.cursor = 'pointer';
        });
        row.addEventListener('click', (e) => {
            const targetCell = e.target.closest('td');
            if (!targetCell) return;

            const index = Array.from(row.children).indexOf(targetCell);
            if (index === 0) return;
            openModal('personal');
        });
    });
});

document.addEventListener("DOMContentLoaded", () => {
    const eduFee = document.getElementById("eduFee");
    const bookFee = document.getElementById("bookFee");
    const priceDiv = document.querySelector(".price");
    const priceInput = document.querySelector(".edu-input");

    function updatePriceState() {
        if (eduFee.checked) {
            priceDiv.classList.add("disabled");
            priceDiv.classList.remove("enabled");
            priceInput.value = "";
        } else {
            priceDiv.classList.remove("disabled");
            priceDiv.classList.add("enabled");
        }
    }

    function formatNumber(value) {
        const num = value.replace(/[^0-9]/g, "");
        if (!num) return "";
        return num.replace(/\B(?=(\d{3})+(?!\d))/g, ",");
    }

    priceInput.addEventListener("input", (e) => {
        const cursorPos = e.target.selectionStart;
        const formatted = formatNumber(e.target.value);
        e.target.value = formatted;
        e.target.setSelectionRange(e.target.value.length, e.target.value.length);
    });

    eduFee.addEventListener("change", updatePriceState);
    bookFee.addEventListener("change", updatePriceState);

    updatePriceState();
});


const now = new Date();

const yy = String(now.getFullYear()).slice(-2); // 뒤 두 자리
const MM = String(now.getMonth() + 1).padStart(2, '0'); // 월 (0부터 시작하므로 +1)
const dd = String(now.getDate()).padStart(2, '0'); // 일
const HH = String(now.getHours()).padStart(2, '0'); // 시
const mm = String(now.getMinutes()).padStart(2, '0'); // 분

const formatted = `${yy}${MM}${dd}${HH}${mm}`;
const bill_id = "3208800028" + formatted;
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
            callbackURL: "https://f57dded7b1fc.ngrok-free.app/pay/callback"
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
                if (data.code === "0000") {
                    alert('청구서가 발행되었습니다.')
                }
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