/* global CryptoJS */

document.addEventListener('DOMContentLoaded', () => {
    const monthInput = document.querySelector('.hidden-picker');
    const monthBtn = document.querySelector('.calendar-open');
    const monthDisplay = document.querySelector('.day-display');
    const teacherSelect = document.getElementById('student-filter');
    const tbody = document.getElementById('student-tbody');

    initCurrentMonth();
    bindSelectAllCheckbox();

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

    // 체크박스 전체 선택
    function bindSelectAllCheckbox() {
        const selectAll = document.querySelector('#pay-edu-select-all');
        const checkboxes = document.querySelectorAll('#student-tbody .row-checkbox');

        if (!selectAll) return;

        // 전체 선택 클릭 시
        selectAll.addEventListener('change', () => {
            checkboxes.forEach(chk => (chk.checked = selectAll.checked));
        });

        // 개별 체크박스 상태 변경 시 헤더 체크박스 상태 갱신
        checkboxes.forEach(chk => {
            chk.addEventListener('change', () => {
                const allChecked = [...checkboxes].every(c => c.checked);
                selectAll.checked = allChecked;
            });
        });
    }

    // 월 변경
    async function onMonthChange() {
        const date = new Date(monthInput.value);
        if (isNaN(date)) return;

        const year = date.getFullYear();
        const month = date.getMonth() + 1;

        monthDisplay.childNodes[0].textContent = `${year}년 ${month}월`;

        const teacherCode = teacherSelect.value || null;
        await fetchStudents(year, month, teacherCode);
    }

    // 선생님 변경
    async function onTeacherChange() {
        const date = new Date(monthInput.value);
        if (isNaN(date)) return;

        const year = date.getFullYear();
        const month = date.getMonth() + 1;
        const teacherCode = teacherSelect.value || null;

        await fetchStudents(year, month, teacherCode);
    }

    // 학생 조회
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

            const students = result.response || result;

            renderStudentTable(students);

        } catch (error) {
            console.error("학생 목록 로드 실패:", error);
            alert("서버와의 통신에 실패했습니다.");
        }
    }

    // 랜더링
    function renderStudentTable(students) {
        const tbody = document.querySelector('#student-tbody');
        tbody.innerHTML = '';

        if (!students || students.length === 0) {
            tbody.innerHTML = `
                <tr><td colspan="8" style="text-align:center;">등록된 학생 데이터가 없습니다.</td></tr>
            `;
            return;
        }

        students.forEach((student, index) => {
            const tr = document.createElement('tr');

            tr.dataset.parentPhone = student.parentPhone || '';
            tr.dataset.studentId = student.studentId || '';
            tr.dataset.studentName = student.studentName || '';
            tr.dataset.totalPrice = student.totalPrice || 0;
            tr.dataset.totalFee = student.totalFee || 0;
            tr.dataset.totalMaterialFee = student.totalMaterialFee || 0;

            const formattedPrice = Number(student.totalPrice || 0).toLocaleString();

            const hanTeacherText = student.hanTeacher ? `${student.hanTeacher}(한)` : '';
            const bookTeacherText = student.bookTeacher ? `${student.bookTeacher}(독)` : '';
            const teacherSeparator = hanTeacherText && bookTeacherText ? ', ' : '';
            const teacherText = `${hanTeacherText}${teacherSeparator}${bookTeacherText}`;

            let statusHtml = '';
            const eduIssued = student.eduStatus === 'issued';
            const materialIssued = student.materialStatus === 'issued';

            if (!eduIssued && !materialIssued) {
                statusHtml = `<span class="unissued">미발행</span>`;
            } else if (eduIssued && !materialIssued) {
                statusHtml = `<span class="edu-issued">발행(교육)</span>`;
            } else if (!eduIssued && materialIssued) {
                statusHtml = `<span class="material-issued">발행(교재)</span>`;
            } else {
                statusHtml = `<span class="issued">발행</span>`;
            }

            tr.innerHTML = `
            <td class="checkbox-group">
                <input type="checkbox" class="row-checkbox" value="${student.studentId}">
            </td>
            <td>${index + 1}</td>
            <td>${student.studentName || '-'}</td>
            <td>${student.subject || '-'}</td>
            <td>${teacherText}</td>
            <td class="charge">${formattedPrice}</td>
            <td>${statusHtml}</td>
            <td><div class="pay-box">-</div></td>
        `;

            tbody.appendChild(tr);
        });
        bindSelectAllCheckbox();
    }
});

// ========== 청구서 유효기간 세팅 ========== //
document.addEventListener('DOMContentLoaded', () => {
    const expireInput = document.querySelector('.expire-input');
    const expireBtn = document.querySelector('.expire-btn');
    const expireDisplay = document.querySelector('.day-picker .day-display');
    const now = new Date();
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

function generateSendHash(billId, phone, price) {
    const input = `${billId},${phone},${price}`;
    return CryptoJS.SHA256(input).toString(CryptoJS.enc.Hex);
}

function generateCancelHash(billId, price) {
    const input = `${billId},${price}`;
    return CryptoJS.SHA256(input).toString(CryptoJS.enc.Hex);
}

// ========== 우측 버튼 클릭 ========== //
document.addEventListener("DOMContentLoaded", () => {
    const payIssue = document.querySelector('#pay-issue');
    const payCancel = document.querySelector('#pay-cancel');
    const payDestroy = document.querySelector('#pay-destory');
    const payReissue = document.querySelector('#pay-reissue')


    // 청구서 발행 버튼
    payIssue.addEventListener('click', async () => {
        // 체크된 학생
        const checkedBoxes = document.querySelectorAll('#student-tbody input[type="checkbox"]:checked');

        if (checkedBoxes.length === 0) {
            alert('학생을 선택하세요.');
            return;
        }

        const eduChecked = document.querySelector('input[name="eduFee"]').checked;
        const bookChecked = document.querySelector('input[name="bookFee"]').checked;
        if (!eduChecked && !bookChecked) {
            alert('청구 종류를 선택하세요.');
            return;
        }
        const selectedMonth = document.querySelector('.hidden-date.hidden-picker').value;
        const [year, month] = selectedMonth.split("-");
        const yy = year;
        const mm = month.padStart(2, "0");
        for (const [index, box] of checkedBoxes.entries()) {
            const row = box.closest('tr');
            const issuedCell = row.querySelector('.unissued, .issued');
            if (issuedCell && issuedCell.classList.contains('issued')) {
                alert(`⚠️ ${row.dataset.studentName} 학생의 ${mm}월 청구서는 이미 발행되었습니다.`);
                continue;
            }
            const studentId = row.dataset.studentId;
            const studentName = row.dataset.studentName;
            const phone = row.dataset.parentPhone;
            const price = row.dataset.totalPrice;
            const totalFee = row.dataset.totalFee;
            const totalMaterialFee = row.dataset.totalMaterialFee;
            const message = document.querySelector('input[name="message"]').value;

            const now = new Date();
            const baseDate = new Date(2025, 0, 1);
            const diffDays = Math.floor((now - baseDate) / (1000 * 60 * 60 * 24));
            const secondsOfDay = now.getHours() * 3600 + now.getMinutes() * 60 + now.getSeconds();

            const dayCode = diffDays.toString(36).padStart(3, "0");
            const timeCode = secondsOfDay.toString(36).padStart(4, "0");

            const indexStr = String(index).padStart(2, "0");
            let statusType = '';
            if (eduChecked && totalFee > 0) {
                const billIdEdu = "3208800028" + dayCode + timeCode + indexStr + "1";
                const sendHashEdu = generateSendHash(billIdEdu, phone, totalFee);
                statusType = 'edu';

                const billEdu = {
                    bill_id: billIdEdu,
                    product_nm: "교육비",
                    message: message || `${studentName} 교육비 청구`,
                    member_nm: studentName,
                    phone: phone,
                    price: totalFee,
                    hash: sendHashEdu,
                    expire_dt: document.querySelector('.expire-input').value,
                    callbackURL: "https://f6d1288ac652.ngrok-free.app/pay/callback"
                    // callbackURL: "https://hohocenter.co.kr/pay/callback"

                };

                await sendBill(billEdu, "EDU");
            }

            if (bookChecked && totalMaterialFee > 0) {
                const billIdBook = "3208800028" + dayCode + timeCode + indexStr + "0";
                const sendHashBook = generateSendHash(billIdBook, phone, totalMaterialFee);
                statusType = 'material'

                const billBook = {
                    bill_id: billIdBook,
                    product_nm: "교재비",
                    message: message || `${studentName} 교재비 청구`,
                    member_nm: studentName,
                    phone: phone,
                    price: totalMaterialFee,
                    hash: sendHashBook,
                    expire_dt: document.querySelector('.expire-input').value,
                    callbackURL: "https://2b08de231333.ngrok-free.app/pay/callback"
                    // callbackURL: "https://hohocenter.co.kr/pay/callback"
                };

                await sendBill(billBook, "BOOK");
            }

            async function sendBill(bill, type) {
                const requestBody = {
                    apikey: "TEST-API-KEY-TALK",
                    member: "TEST-MEMBER-FOR-API",
                    merchant: "TEST-MERCHANT-FOR-API",
                    bill
                };
                console.log(JSON.stringify(requestBody));
                try {
                    const res = await fetch("https://stg.paymint.co.kr/partner/if/bill/send", {
                        method: "POST",
                        headers: {"Content-Type": "application/json", "Accept": "application/json"},
                        body: JSON.stringify(requestBody)
                    });

                    const data = await res.json();
                    console.log(`📨 [${type}]`, bill.bill_id, data);

                    if (data.code === "0000") {
                        const saveBody = {
                            billId: requestBody.bill.bill_id,
                            productName: requestBody.bill.product_nm,
                            message: requestBody.bill.message,
                            studentName: requestBody.bill.studentName,
                            studentId: studentId,
                            amount: requestBody.bill.price,
                            statusType: statusType,
                            requestDate: now.toISOString().split("T")[0],
                            expireDate: requestBody.bill.expire_dt,
                            yy: yy,
                            mm: mm
                        };
                        const res = await fetch("/pay/history/insert", {
                            method: "POST",
                            headers: {"Content-Type": "application/json", "Accept": "application/json"},
                            body: JSON.stringify(saveBody)
                        });

                        const data = await res.json();
                        console.log(data);

                    } else {
                        alert(`❌ ${bill.member_nm} ${type} 청구 실패: ${data.msg || '서버 오류'}`);
                    }
                } catch (err) {
                    console.error(`❌ ${type} 청구 중 오류:`, err);
                }
            }
        }

        alert("✅ 모든 청구가 완료되었습니다.");
        window.location.reload();
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