/* global CryptoJS */

document.addEventListener('DOMContentLoaded', () => {
    const monthInput = document.querySelector('.hidden-picker');
    const monthBtn = document.querySelector('.calendar-open');
    const monthDisplay = document.querySelector('.day-display');
    const teacherSelect = document.getElementById('student-filter');
    const tbody = document.getElementById('student-tbody');

    initCurrentMonth();

    monthBtn.addEventListener('click', () => monthInput.showPicker());
    monthInput.addEventListener('change', onMonthChange);
    teacherSelect.addEventListener('change', onTeacherChange);

    function initCurrentMonth() {
        const now = new Date();
        const year = now.getFullYear();
        const month = now.getMonth() + 1;

        monthInput.value = `${year}-${String(month).padStart(2, '0')}`;
        monthDisplay.insertAdjacentText('afterbegin', `${year}년 ${month}월`);

    }

    async function onMonthChange() {
        const date = new Date(monthInput.value);
        if (isNaN(date)) return;

        const year = date.getFullYear();
        const month = date.getMonth() + 1;

        monthDisplay.childNodes[0].textContent = `${year}년 ${month}월`;

        const teacherCode = teacherSelect.value || null;
        await fetchStudents(year, month, teacherCode);
    }

    async function onTeacherChange() {
        const date = new Date(monthInput.value);
        if (isNaN(date)) return;

        const year = date.getFullYear();
        const month = date.getMonth() + 1;
        const teacherCode = teacherSelect.value || null;

        await fetchStudents(year, month, teacherCode);
    }

    async function fetchStudents(year, month, teacherCode) {
        const requestBody = {
            year: year,
            month: String(month).padStart(2, '0'),
            userCode: teacherCode
        };

        try {
            const response = await fetch("/pay/students", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    "Accept": "application/json"
                },
                body: JSON.stringify(requestBody),
            });

            if (!response.ok) throw new Error("서버 오류 발생");

            const result = await response.json();
            console.log(JSON.stringify(result, null, 2));

            const students = result.response || result;
            renderStudentTable(students);

        } catch (error) {
            console.error("학생 목록 로드 실패:", error);
            alert("서버와의 통신에 실패했습니다.");
        }
    }

    function renderStudentTable(students) {
        tbody.innerHTML = '';

        if (!students || students.length === 0) {
            tbody.innerHTML = `
                <tr><td colspan="8" style="text-align:center;">해당 조건의 학생 데이터가 없습니다.</td></tr>
            `;
            return;
        }

        students.forEach((student, index) => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td class="checkbox-group">
                    <input type="checkbox" value="${student.studentId}">
                </td>
                <td>${index + 1}</td>
                <td>${student.studentName}</td>
                <td>${student.subject}</td>
                <td class="cal-content">-</td>
                <td class="charge">-</td>
                <td><span class="unissued">-</span></td>
                <td><div class="pay-box">-</div></td>
            `;
            tbody.appendChild(tr);
        });
    }
});

// ========== 청구서 유효기간 세팅 ========== //
document.addEventListener('DOMContentLoaded', () => {
    const expireInput = document.querySelector('.expire-input');
    const expireBtn = document.querySelector('.expire-btn');
    const expireDisplay = document.querySelector('.day-picker .day-display');

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

    expireBtn.addEventListener('click', () => {
        expireInput.showPicker();
    });

    expireInput.addEventListener('change', () => {
        const date = new Date(expireInput.value);
        if (isNaN(date)) return;
        expireDisplay.textContent = `${date.getFullYear()}년 ${date.getMonth() + 1}월 ${date.getDate()}일`;
    });
});

// ========== 모달 열기 ========== //
document.addEventListener('DOMContentLoaded', () => {

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

// ========== 금액 직접 입력하기 ========== //
document.addEventListener('DOMContentLoaded', () => {
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


// 이 부분은 청구서 발행 전에 수정되어야 함 지금은 임의 값으로 세팅해둠
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

function generateSendHash(billId, phone, price) {
    const input = `${billId},${phone},${price}`;
    return CryptoJS.SHA256(input).toString(CryptoJS.enc.Hex);
}

function generateCancelHash(billId, price) {
    const input = `${billId},${price}`;
    return CryptoJS.SHA256(input).toString(CryptoJS.enc.Hex);
}

sendHash = generateSendHash(bill_id, phone, price);
cancelHash = generateCancelHash(bill_id, price);

console.log("✅ sendHash:", sendHash);
console.log("✅ cancelHash:", cancelHash);

// ========== 우측 버튼 클릭 ========== //
document.addEventListener("DOMContentLoaded", () => {
    const payIssue = document.querySelector('#pay-issue');
    const payCancel = document.querySelector('#pay-cancel');
    const payDestroy = document.querySelector('#pay-destory');
    const payReissue = document.querySelector('#pay-reissue')
    if (!payIssue) return;


    // 청구서 발행 버튼
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

        fetch("https://stg.paymint.co.kr/partner/if/bill/send", {
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
                } else if (data.code === "9800") {
                    alert('이미 발행된 청구서 입니다.')
                } else if (data.code === "9999") {
                    alert(data.msg);
                }
            })
            .catch(err => {
                console.error("오류 발생:", err);
            });
    });

    // 결제 취소 버튼
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

        fetch("https://stg.paymint.co.kr/partner/if/bill/cancel", {
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
                } else if (data.code === "9980") {
                    alert('청구서를 찾을 수 없습니다.');
                } else if (data.code === "9999") {
                    alert(data.msg);
                }
            })
            .catch(err => {
                console.error("오류 발생:", err);
            });
    });

    // 청구서 파기 버튼
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

        fetch("https://stg.paymint.co.kr/partner/if/bill/destroy", {
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
                    alert('청구서가 파기되었습니다.');

                } else if (data.code === "9980") {
                    alert('청구서를 찾을 수 없습니다.');

                } else if (data.code === "9999") {
                    alert(data.msg);
                }
            })
            .catch(err => {
                console.error("오류 발생:", err);
            });
    });

    // 청구서 재발행 버튼
    payReissue.addEventListener('click', () => {
        console.log('재발행 버튼 ');

        const requestBody = {
            apikey: "TEST-API-KEY-TALK",
            member: "TEST-MEMBER-FOR-API",
            merchant: "TEST-MERCHANT-FOR-API",
            bill_id: bill_id
        };

        fetch("https://stg.paymint.co.kr/partner/if/bill/resend", {
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
                    alert('청구서가 발행되었습니다.')
                }
                if (data.code === "9800") {
                    alert('이미 발행된 청구서 입니다.')
                }
                if (data.code === "9980") {
                    alert('청구서를 찾을 수 없습니다.');
                }
            })
            .catch(err => {
                console.error("오류 발생:", err);
            });
    });

});