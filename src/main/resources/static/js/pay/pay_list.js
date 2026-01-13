let unpaidStudents = [];
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

    const openCashbillModal = () => {
        closeAllModals();
        modalCashbill.style.display = "block";
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