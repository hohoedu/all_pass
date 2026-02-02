let unpaidStudents = [];
let cashPaymentStudents = [];
let selectedYear = '';
let selectedMonth = '';

document.addEventListener("DOMContentLoaded", () => {

    /* -----------------------------
        DOM 캐싱
    ----------------------------- */
    const monthInput = document.querySelector(".hidden-date");
    const calendarBtn = document.querySelector(".calendar-open");
    const currentMonth = document.querySelector(".current-month");

    const btnAddPayment = document.querySelector("#btn-add-payment");
    const btnAddCashbill = document.querySelector("#btn-add-cashbill");

    const modalPayment = document.querySelector(".add-payment-modal");
    const modalCashbill = document.querySelector(".add-cashbill-modal");

    const studentTableBody = document.querySelector("#unpaid-student-list");

    const cashbillStudentTableBody = document.querySelector("#cashbill-student-list");
    const cashbillSearchInput = document.querySelector(".add-cashbill-modal .add-inform .basic-input");
    const cashbillSearchBtn = document.querySelector("#search-cashbill-student");

    const prepayAddBtn = document.querySelector(".charge-add");
    const manualTableBody = document.querySelector(".manual-payment-table tbody");

    const searchInput = document.querySelector(".student-content .basic-input");
    const searchBtn = document.querySelector("#search-unpaid-student");


    /* -----------------------------
        유틸 함수
    ----------------------------- */
    const formatKoreanDate = (dateStr) => {
        if (!dateStr) return '';
        const [y, m, d] = dateStr.split('-');
        return `${y}년 ${Number(m)}월 ${Number(d)}일`;
    };

    const getTodayForDateInput = () => {
        const d = new Date();
        const yy = d.getFullYear();
        const mm = String(d.getMonth() + 1).padStart(2, '0');
        const dd = String(d.getDate()).padStart(2, '0');
        return `${yy}-${mm}-${dd}`;
    };

    const closeAllModals = () =>
        document.querySelectorAll(".modal").forEach(m => m.style.display = "none");


    /* -----------------------------
        날짜 초기화 + 변경
    ----------------------------- */

    // ===== 1. initMonth 함수를 이렇게 수정하세요 =====

    // ===== 1. initMonth 함수를 이렇게 수정하세요 =====


    function initMonth() {
        // URL 파라미터에서 년월 가져오기
        const urlParams = new URLSearchParams(window.location.search);
        const urlYear = urlParams.get('year');
        const urlMonth = urlParams.get('month');

        let yy, mm;

        if (urlYear && urlMonth) {
            // URL 파라미터가 있으면 그대로 사용
            yy = urlYear;
            mm = String(urlMonth).padStart(2, "0");
        } else {
            // 없으면 오늘 날짜 +1개월
            const now = new Date();
            const nextMonth = new Date(now.getFullYear(), now.getMonth() + 1, 1);
            yy = nextMonth.getFullYear();
            mm = String(nextMonth.getMonth() + 1).padStart(2, "0");
        }

        if (monthInput) monthInput.value = `${yy}-${mm}`;
        if (currentMonth) currentMonth.textContent = `${yy}년 ${parseInt(mm)}월`;

        selectedYear = yy;
        selectedMonth = mm;
    }


    const handleMonthChange = () => {
        if (!monthInput || !currentMonth) return;
        const [year, month] = monthInput.value.split("-");
        currentMonth.textContent = `${year}년 ${parseInt(month)}월`;

        const baseUrl = window.location.origin + window.location.pathname;
        window.location.href = `${baseUrl}?year=${year}&month=${month}`;
    };

    const initPayDate = () => {
        const input = document.getElementById('manual-pay-date');
        const text = document.getElementById('pay-date-text');
        if (!input || !text) return;

        if (!input.value) {
            input.value = getTodayForDateInput();
        }

        text.textContent = formatKoreanDate(input.value);
    };

    monthInput?.addEventListener("change", handleMonthChange);
    calendarBtn?.addEventListener("click", () => monthInput.showPicker());
    initMonth();

    document.addEventListener("click", e => {
        const btn = e.target.closest(".calendar-open");
        if (!btn) return;

        const targetId = btn.dataset.target;
        const input = document.getElementById(targetId);
        if (!input) return;

        if (typeof input.showPicker === "function") {
            input.showPicker();
        } else {
            input.focus();
            input.click();
        }
    });

    /* -----------------------------
        학생 검색
    ----------------------------- */

    const renderStudentList = list => {
        if (!studentTableBody) return;
        studentTableBody.innerHTML = "";

        if (!list.length) {
            studentTableBody.innerHTML =
                `<tr><td colspan="3" class="empty">결제 가능 학생이 없습니다.</td></tr>`;
            return;
        }

        list.forEach(s => {
            const tr = document.createElement("tr");
            tr.dataset.billId = s.billId;
            tr.dataset.studentId = s.studentId;
            tr.dataset.paymentKey = s.paymentKey;

            tr.innerHTML = `
                <td>${s.studentName}</td>
                <td>${s.gradeName || "-"}</td>
                <td>${[s.hanTeacher, s.bookTeacher].filter(v => v?.trim()).join(",")}</td>
            `;

            /* 학생 클릭 시 우측 학생명 변경 */
            tr.addEventListener("click", () => {

                // 기존 선택 제거
                document.querySelectorAll("#unpaid-student-list tr").forEach(r => r.classList.remove("selected"));

                // 현재 선택 추가
                tr.classList.add("selected");

                // 우측 학생명 변경
                const nameCell = document.querySelector("#paid-student td:nth-child(2)");
                if (nameCell) nameCell.textContent = s.studentName;
                const amountCell = document.querySelector("#paid-student td:nth-child(4)");
                if (amountCell) amountCell.textContent = s.amount;
            });

            studentTableBody.appendChild(tr);
        });
    };

    const filterStudents = keyword => {
        keyword = keyword.trim();

        if (!keyword) {
            renderStudentList(unpaidStudents);
            return;
        }

        const filtered = unpaidStudents.filter(s =>
            s.studentName.includes(keyword) ||
            s.gradeName?.includes(keyword) ||
            s.hanTeacher?.includes(keyword) ||
            s.bookTeacher?.includes(keyword)
        );

        renderStudentList(filtered);
    };

    searchInput.addEventListener("keydown", e => {
        if (e.key === "Enter") filterStudents(searchInput.value);
    });

    searchBtn.addEventListener("click", () => filterStudents(searchInput.value));

    /* -----------------------------
          현금영수증 학생 목록 렌더링
    ----------------------------- */
    const renderCashbillStudentList = list => {
        if (!cashbillStudentTableBody) return;
        cashbillStudentTableBody.innerHTML = "";

        if (!list.length) {
            cashbillStudentTableBody.innerHTML =
                `<tr><td colspan="3" style="text-align: center; padding: 20px;">현금/계좌이체 결제 내역이 없습니다.</td></tr>`;
            return;
        }

        list.forEach((s, index) => {
            const tr = document.createElement("tr");
            tr.style.cursor = "pointer";
            tr.dataset.index = index;
            tr.dataset.studentId = s.studentId;
            tr.dataset.paymentKey = s.paymentKey;
            tr.dataset.amount = s.amount;
            tr.dataset.phoneNumber = s.phoneNumber || '';

            tr.innerHTML = `
                <td>${s.studentName}</td>
                <td>${s.gradeName || "-"}</td>
                <td>${[s.hanTeacher, s.bookTeacher].filter(v => v?.trim()).join(",") || "-"}</td>
            `;

            // 클릭 이벤트
            tr.addEventListener("click", () => {
                // 기존 선택 해제
                document.querySelectorAll("#cashbill-student-list tr").forEach(r => {
                    r.classList.remove("selected");
                    r.style.backgroundColor = "";
                });

                // 현재 선택
                tr.classList.add("selected");
                tr.style.backgroundColor = "#e3f2fd";

                // 선택된 학생 정보 저장 (전역 변수에)
                window.selectedCashbillStudent = {
                    studentId: s.studentId,
                    paymentKey: s.paymentKey,
                    name: s.studentName,
                    grade: s.gradeName,
                    teacher: [s.hanTeacher, s.bookTeacher].filter(v => v?.trim()).join(","),
                    amount: s.amount,
                    phoneNumber: s.phoneNumber || '' // 전화번호 저장
                };

                // 우측 정보 표시
                const nameCell = document.getElementById("student-name-cell");
                const amountCell = document.getElementById("cash-amount-cell");
                if (nameCell) nameCell.textContent = s.studentName;
                if (amountCell) amountCell.textContent = parseInt(s.amount).toLocaleString() + "원";

                // 총금액에 자동 입력
                const cashPriceInput = document.getElementById("cash-price");
                if (cashPriceInput) {
                    cashPriceInput.value = parseInt(s.amount).toLocaleString();
                    cashPriceInput.dispatchEvent(new Event('input'));
                }

                // 발급번호에 전화번호 자동 입력 (개인 선택 시)
                const receiptNumberInput = document.getElementById("receipt-number");
                const receiptType = document.querySelector('input[name="receipt-type"]:checked').value;

                if (receiptNumberInput && s.phoneNumber && receiptType === 'personal') {
                    // 전화번호 포맷팅 (하이픈 제거)
                    const cleanPhone = s.phoneNumber.replace(/[^0-9]/g, '');
                    receiptNumberInput.value = cleanPhone;
                }
            });

            cashbillStudentTableBody.appendChild(tr);
        });
    };

    const filterCashbillStudents = keyword => {
        keyword = keyword.trim();

        if (!keyword) {
            renderCashbillStudentList(cashPaymentStudents);
            return;
        }

        const filtered = cashPaymentStudents.filter(s =>
            s.studentName.includes(keyword) ||
            s.gradeName?.includes(keyword) ||
            s.hanTeacher?.includes(keyword) ||
            s.bookTeacher?.includes(keyword)
        );

        renderCashbillStudentList(filtered);
    };

    cashbillSearchInput?.addEventListener("keydown", e => {
        if (e.key === "Enter") filterCashbillStudents(cashbillSearchInput.value);
    });

    cashbillSearchBtn?.addEventListener("click", () => filterCashbillStudents(cashbillSearchInput.value));

    /* -----------------------------
        모달 열기: 수기 결제 추가
    ----------------------------- */
    document.getElementById('manual-pay-date')?.addEventListener('change', e => {
        const text = document.getElementById('pay-date-text');
        if (!text) return;

        text.textContent = formatKoreanDate(e.target.value);
    });
    const openAddPaymentModal = async () => {
        try {
            closeAllModals();
            const yearMonth = {
                year: monthInput.value.split("-")[0],
                month: monthInput.value.split("-")[1]
            }
            const res = await fetch("/pay/list/students", {
                method: "POST",
                headers: {"Content-Type": "application/json"},
                body: JSON.stringify(yearMonth)
            });

            if (!res.ok) throw new Error("학생 조회 실패");

            const students = await res.json();
            unpaidStudents = students.response || [];
            initPayDate();
            renderStudentList(unpaidStudents);

            const payDateInput = document.getElementById("manual-pay-date");
            if (payDateInput) {
                payDateInput.value = getTodayForDateInput();
            }

            modalPayment.style.display = "block";

        } catch (err) {
            alert("납부내역 추가 중 오류가 발생했습니다.");
        }
    };

    btnAddPayment?.addEventListener("click", openAddPaymentModal);


    document.querySelector("#save-pay").addEventListener("click", async () => {
        const payDateInput = document.getElementById("manual-pay-date");
        const selectedRow = document.querySelector("#unpaid-student-list tr.selected");
        if (!selectedRow) {
            alert("학생을 선택하세요.");
            return;
        }
        const cardAmount = Number(document.querySelector('.border-green')?.value || 0);
        const cardCode = document.getElementById('cardCodeSelect')?.value || '';
        console.log('card code = ' + cardCode);
        if (cardAmount > 0 && !cardCode) {
            alert("카드사를 선택해주세요.");
            return;
        }

        const dto = {
            studentId: selectedRow.dataset.studentId,
            paymentKey: selectedRow.dataset.paymentKey,
            billId: selectedRow.dataset.billId,
            paidDate: payDateInput.value,
            cardName: cardCode,
            yy: monthInput.value.split("-")[0],
            mm: monthInput.value.split("-")[1],
            cardAmount: Number(document.querySelector('.pay-edu-table .border-green')?.value || 0),
            cashAmount: Number(document.querySelector('.pay-edu-table .border-blue')?.value || 0),
            transferAmount: Number(document.querySelector('.pay-edu-table .border-olive')?.value || 0),
        };
        try {
            const res = await fetch("/pay/manual", {
                method: "POST",
                headers: {"Content-Type": "application/json"},
                body: JSON.stringify(dto)
            });

            const result = await res.json();

            if (result.success) {

                alert("수기 결제가 완료되었습니다.");

                location.reload();
            } else {
                alert("수기 결제 실패");
            }
        } catch (err) {
            alert("오류가 발생했습니다.");
        }
    });

    /* -----------------------------
        모달 열기: 현금영수증
    ----------------------------- */
    const openCashbillModal = async () => {
        try {
            closeAllModals();

            // 현금/계좌이체 결제 학생 목록 조회
            const yearMonth = {
                year: monthInput.value.split("-")[0],
                month: monthInput.value.split("-")[1]
            };

            const res = await fetch("/pay/api/cashbill/students", {
                method: "POST",
                headers: {"Content-Type": "application/json"},
                body: JSON.stringify(yearMonth)
            });

            if (!res.ok) throw new Error("현금영수증 발급 대상 학생 조회 실패");

            const result = await res.json();
            cashPaymentStudents = result.response || [];

            // 학생 목록 렌더링
            renderCashbillStudentList(cashPaymentStudents);

            // 검색 입력 초기화
            if (cashbillSearchInput) {
                cashbillSearchInput.value = '';
            }

            modalCashbill.style.display = "block";

        } catch (err) {
            console.error(err);
            alert("현금영수증 모달을 여는 중 오류가 발생했습니다.");
        }
    };

    btnAddCashbill?.addEventListener("click", openCashbillModal);


    /* -----------------------------
        선결제 행 추가/삭제 토글
    ----------------------------- */

    // const togglePrepayRow = () => {
    //     if (!manualTableBody) return;
    //
    //     const existingRow = manualTableBody.querySelector(".prepay-row");
    //     if (existingRow) {
    //         existingRow.remove();
    //         return;
    //     }
    //
    //     const newRow = document.createElement("tr");
    //     newRow.classList.add("prepay-row");
    //     newRow.innerHTML = `
    //         <td class="label-cell">선결제</td>
    //         <td class="content-cell white-bg" colspan="2">
    //             <div class="prepay-input-group">
    //                 <div class="prepay-period">
    //                     <span>기간 선택 :</span>
    //                     ${[2, 3, 4, 5, 6].map(m => `<label><input type="radio" name="prepay-period" value="${m}"> ${m}개월</label>`).join("")}
    //                 </div>
    //                 <div class="prepay-month-row">
    //                     <label>시작 월</label>
    //                     <input type="month" class="prepay-start-month">
    //                 </div>
    //                 <div class="prepay-note-row">
    //                     <label>비고</label>
    //                     <input type="text" class="prepay-note" placeholder="비고 입력">
    //                 </div>
    //             </div>
    //         </td>
    //     `;
    //
    //     manualTableBody.appendChild(newRow);
    // };
    //
    // prepayAddBtn?.addEventListener("click", togglePrepayRow);


});


///////////////
// 현금영수증  //
//////////////
// 과세/비과세 구분 및 금액 자동 계산
document.addEventListener('DOMContentLoaded', function () {
    const taxRadios = document.querySelectorAll('input[name="tax-type"]');
    const receiptTypeRadios = document.querySelectorAll('input[name="receipt-type"]');
    const cashPriceInput = document.getElementById('cash-price');
    const supplyPriceInput = document.getElementById('supply-price');
    const taxPriceInput = document.getElementById('tax-price');
    const receiptNumberInput = document.getElementById('receipt-number');
    const receiptDateInput = document.getElementById('receipt-date');
    const receiptDateText = document.getElementById('receipt-date-text');
    const calendarBtn = document.querySelector('.calendar-open[data-target="receipt-date"]');
    const issueCashbillBtn = document.getElementById('issue-cashbill');

    // 자진발급 이전의 발급번호 저장용 변수
    let savedReceiptNumber = '';

    // 달력 아이콘 클릭 시 date input 열기
    if (calendarBtn) {
        calendarBtn.addEventListener('click', function () {
            receiptDateInput.showPicker();
        });
    }

    // 날짜 선택 시 텍스트 업데이트
    receiptDateInput.addEventListener('change', function () {
        if (this.value) {
            const date = new Date(this.value);
            const year = date.getFullYear();
            const month = date.getMonth() + 1;
            const day = date.getDate();
            receiptDateText.textContent = `${year}년 ${month}월 ${day}일`;
        }
    });

    // 페이지 로드 시 오늘 날짜로 초기화
    const today = new Date();
    const yyyy = today.getFullYear();
    const mm = String(today.getMonth() + 1).padStart(2, '0');
    const dd = String(today.getDate()).padStart(2, '0');
    receiptDateInput.value = `${yyyy}-${mm}-${dd}`;
    receiptDateText.textContent = `${yyyy}년 ${today.getMonth() + 1}월 ${today.getDate()}일`;

    // 발급구분 변경 시 발급번호 처리
    receiptTypeRadios.forEach(radio => {
        radio.addEventListener('change', function () {
            if (this.value === 'self') {
                if (receiptNumberInput.value !== '0100001234') {
                    savedReceiptNumber = receiptNumberInput.value;
                }
                receiptNumberInput.value = '0100001234';
                receiptNumberInput.readOnly = true;
                receiptNumberInput.style.backgroundColor = '#f5f5f5';
            } else {
                if (receiptNumberInput.value === '0100001234') {
                    receiptNumberInput.value = savedReceiptNumber;
                }
                receiptNumberInput.readOnly = false;
                receiptNumberInput.style.backgroundColor = '';
                receiptNumberInput.placeholder = '휴대폰번호(01012345678) 또는 사업자번호';
            }
        });
    });

    // 발급번호 입력 시 유효성 검사
    receiptNumberInput.addEventListener('blur', function () {
        validateReceiptNumber();
    });

    function validateReceiptNumber() {
        const receiptType = document.querySelector('input[name="receipt-type"]:checked').value;
        const receiptNumber = receiptNumberInput.value.replace(/[^0-9]/g, '');

        if (receiptType === 'self' || !receiptNumber) {
            return true;
        }

        const isPhoneNumber = /^010\d{7,8}$/.test(receiptNumber);
        const isBusinessNumber = /^\d{10}$/.test(receiptNumber) && !receiptNumber.startsWith('010');

        if (receiptType === 'personal' && !isPhoneNumber) {
            return false;
        }

        if (receiptType === 'business' && !isBusinessNumber) {
            return false;
        }

        return true;
    }

    // 총금액 입력 시 천단위 콤마 + 자동 계산
    cashPriceInput.addEventListener('input', function (e) {
        let value = this.value.replace(/[^0-9]/g, '');

        if (value) {
            this.value = parseInt(value).toLocaleString();
        } else {
            this.value = '';
        }

        calculateAmounts();
    });

    // 과세구분 변경 시 자동 재계산
    taxRadios.forEach(radio => {
        radio.addEventListener('change', function () {
            calculateAmounts();
        });
    });

    function calculateAmounts() {
        const totalAmount = parseInt(cashPriceInput.value.replace(/,/g, '')) || 0;

        if (totalAmount === 0) {
            supplyPriceInput.value = '';
            taxPriceInput.value = '';
            return;
        }

        const isTaxable = document.querySelector('input[name="tax-type"]:checked').value === 'taxable';
        let supplyPrice, tax;

        if (isTaxable) {
            supplyPrice = Math.round(totalAmount / 1.1);
            tax = totalAmount - supplyPrice;
        } else {
            supplyPrice = totalAmount;
            tax = 0;
        }

        supplyPriceInput.value = supplyPrice.toLocaleString();
        taxPriceInput.value = tax.toLocaleString();
    }

    // 발행 버튼 클릭 이벤트
    issueCashbillBtn.addEventListener('click', function () {
        if (!validateAllFields()) {
            return;
        }

        const requestData = collectFormData();
        sendCashbillRequest(requestData);
    });

    // 전체 필드 유효성 검사
    function validateAllFields() {
        // window.selectedCashbillStudent 사용
        if (!window.selectedCashbillStudent) {
            alert('학생을 선택해주세요.');
            return false;
        }

        const receiptNumber = receiptNumberInput.value.trim();
        if (!receiptNumber) {
            alert('발급번호를 입력해주세요.');
            receiptNumberInput.focus();
            return false;
        }

        const receiptType = document.querySelector('input[name="receipt-type"]:checked').value;
        const receiptNumberOnly = receiptNumber.replace(/[^0-9]/g, '');

        if (receiptType === 'personal') {
            const isPhoneNumber = /^010\d{7,8}$/.test(receiptNumberOnly);
            if (!isPhoneNumber) {
                alert('개인 소득공제용은 올바른 휴대폰번호(010-XXXX-XXXX)를 입력해주세요.');
                receiptNumberInput.focus();
                return false;
            }
        } else if (receiptType === 'business') {
            const isBusinessNumber = /^\d{10}$/.test(receiptNumberOnly) && !receiptNumberOnly.startsWith('010');
            if (!isBusinessNumber) {
                alert('사업자 지출증빙용은 올바른 사업자등록번호(10자리)를 입력해주세요.');
                receiptNumberInput.focus();
                return false;
            }
        }

        const cashPrice = cashPriceInput.value.replace(/,/g, '').trim();
        if (!cashPrice || parseInt(cashPrice) <= 0) {
            alert('총 금액을 입력해주세요.');
            cashPriceInput.focus();
            return false;
        }

        const supplyPrice = supplyPriceInput.value.replace(/,/g, '').trim();
        if (!supplyPrice) {
            alert('공급가액이 계산되지 않았습니다.');
            return false;
        }

        const taxPrice = taxPriceInput.value.replace(/,/g, '').trim();
        if (taxPrice === '') {
            alert('세금이 계산되지 않았습니다.');
            return false;
        }

        if (!receiptDateInput.value) {
            alert('발행일자를 선택해주세요.');
            return false;
        }

        return true;
    }

    // 폼 데이터 수집
    function collectFormData() {
        const receiptType = document.querySelector('input[name="receipt-type"]:checked').value;
        const taxType = document.querySelector('input[name="tax-type"]:checked').value;

        let traderValue = '0';
        if (receiptType === 'business') {
            traderValue = '1';
        } else if (receiptType === 'self') {
            traderValue = '2';
        }

        return {
            studentId: window.selectedCashbillStudent.studentId,
            paymentKey: window.selectedCashbillStudent.paymentKey,
            studentInfo: {
                name: window.selectedCashbillStudent.name,
                grade: window.selectedCashbillStudent.grade,
                teacher: window.selectedCashbillStudent.teacher
            },
            receiptNumber: receiptNumberInput.value.replace(/[^0-9]/g, ''),
            receiptType: receiptType,
            taxType: taxType,
            trader: traderValue,
            price: cashPriceInput.value.replace(/,/g, ''),
            supplyPrice: supplyPriceInput.value.replace(/,/g, ''),
            tax: taxPriceInput.value.replace(/,/g, ''),
            issueDate: receiptDateInput.value
        };
    }

    // 서버로 POST 요청
    function sendCashbillRequest(data) {
        console.log(data);
        fetch('/pay/api/cashbill/issue', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(data)
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error('서버 응답 오류');
                }
                return response.json();
            })
            .then(result => {
                alert('현금영수증이 발행되었습니다.');
                resetForm();
                // 모달 닫기
                document.querySelector('.add-cashbill-modal').style.display = 'none';
            })
            .catch(error => {
                console.error('Error:', error);
                alert('현금영수증 발행 중 오류가 발생했습니다.');
            });
    }

    // 폼 초기화
    function resetForm() {
        window.selectedCashbillStudent = null;
        document.getElementById('student-name-cell').textContent = '학생을 선택해주세요.';
        document.getElementById('cash-amount-cell').textContent = '-';
        receiptNumberInput.value = '';
        cashPriceInput.value = '';
        supplyPriceInput.value = '';
        taxPriceInput.value = '';
        document.querySelector('input[name="receipt-type"][value="personal"]').checked = true;
        document.querySelector('input[name="tax-type"][value="taxable"]').checked = true;

        // 선택 해제
        document.querySelectorAll('#cashbill-student-list tr').forEach(tr => {
            tr.classList.remove('selected');
            tr.style.backgroundColor = '';
        });
    }
});