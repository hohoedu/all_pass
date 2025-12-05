document.addEventListener("DOMContentLoaded", () => {

    /* ------------------------------
       공통 요소 캐싱
       ------------------------------ */
    const monthInput = document.querySelector(".hidden-date");
    const calendarBtn = document.querySelector(".calendar-open");
    const currentMonth = document.querySelector(".current-month");

    const btnAddPayment = document.querySelector("#btn-add-payment"); // 납부내역 추가 버튼
    const btnAddCashbill = document.querySelector("#btn-add-cashbill"); // 현금영수증 버튼

    const modalPayment = document.querySelector(".add-payment-modal");
    const modalCashbill = document.querySelector(".add-cashbill-modal");

    const studentTableBody = document.querySelector("#student-list");
    const prepayAddBtn = document.querySelector(".charge-add");
    const manualTableBody = document.querySelector(".manual-payment-table tbody");


    /* ------------------------------
       1. 날짜 초기화 + 변경 이벤트
       ------------------------------ */
    function initMonth() {
        const now = new Date();
        const yy = now.getFullYear();
        const mm = String(now.getMonth() + 1).padStart(2, "0");

        if (monthInput) monthInput.value = `${yy}-${mm}`;
        if (currentMonth) currentMonth.textContent = `${yy}년 ${parseInt(mm)}월`;
    }

    function handleMonthChange() {
        if (!monthInput || !currentMonth) return;

        const [year, month] = monthInput.value.split("-");
        currentMonth.textContent = `${year}년 ${parseInt(month)}월`;
    }

    if (calendarBtn) {
        calendarBtn.addEventListener("click", () => monthInput?.showPicker());
    }

    if (monthInput) {
        monthInput.addEventListener("change", handleMonthChange);
    }

    initMonth();


    /* ------------------------------
       공통: 모든 모달 닫기
       ------------------------------ */
    function closeAllModals() {
        document.querySelectorAll(".modal").forEach(m => m.style.display = "none");
    }


    /* ------------------------------
       2. 학생 조회 후 “수기 결제 추가” 모달 열기
       ------------------------------ */
    async function openAddPaymentModal() {
        try {
            closeAllModals();

            const now = new Date();
            const yy = now.getFullYear().toString();
            const mm = String(now.getMonth() + 1).padStart(2, "0");

            const res = await fetch("/pay/list/students", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ yy, mm })
            });

            if (!res.ok) throw new Error("학생 조회 실패");
            const students = await res.json();

            if (studentTableBody) {
                studentTableBody.innerHTML = "";

                if (students.length === 0) {
                    studentTableBody.innerHTML = `
                        <tr><td colspan="3" class="empty">해당 월의 결제 가능 학생이 없습니다.</td></tr>
                    `;
                } else {
                    students.forEach(s => {
                        const tr = document.createElement("tr");
                        tr.dataset.billId = s.billId;
                        tr.dataset.studentId = s.studentId;
                        tr.innerHTML = `
                            <td>${s.studentName}</td>
                            <td>${s.grade || "-"}</td>
                            <td>${s.teacherName || "-"}</td>
                        `;
                        studentTableBody.appendChild(tr);
                    });
                }
            }

            if (modalPayment) modalPayment.style.display = "block";

        } catch (err) {
            alert("납부내역 추가 중 오류가 발생했습니다.");
        }
    }

    if (btnAddPayment) {
        btnAddPayment.addEventListener("click", openAddPaymentModal);
    }

    function openCashbillModal() {
        closeAllModals();
        modalCashbill.style.display = "block";
    }

    if (btnAddCashbill) {
        btnAddCashbill.addEventListener("click", openCashbillModal);
    }

    /* ------------------------------
       3. 선결제 행 추가 / 제거
       ------------------------------ */
    function togglePrepayRow() {
        if (!manualTableBody) return;

        const existingRow = manualTableBody.querySelector(".prepay-row");

        if (existingRow) {
            existingRow.remove();
            return;
        }

        const newRow = document.createElement("tr");
        newRow.classList.add("prepay-row");
        newRow.innerHTML = `
            <td class="label-cell">선결제</td>
            <td class="content-cell white-bg" colspan="2">
                <div class="prepay-input-group">
                    <div class="prepay-period">
                        <span>기간 선택 :</span>
                        <label><input type="radio" name="prepay-period" value="2"> 2개월</label>
                        <label><input type="radio" name="prepay-period" value="3"> 3개월</label>
                        <label><input type="radio" name="prepay-period" value="4"> 4개월</label>
                        <label><input type="radio" name="prepay-period" value="5"> 5개월</label>
                        <label><input type="radio" name="prepay-period" value="6"> 6개월</label>
                    </div>

                    <div class="prepay-month-row">
                        <label>시작 월</label>
                        <input type="month" class="prepay-start-month">
                    </div>

                    <div class="prepay-note-row">
                        <label>비고</label>
                        <input type="text" class="prepay-note" placeholder="비고 입력">
                    </div>
                </div>
            </td>
        `;
        manualTableBody.appendChild(newRow);
    }

    if (prepayAddBtn) {
        prepayAddBtn.addEventListener("click", togglePrepayRow);
    }
});
