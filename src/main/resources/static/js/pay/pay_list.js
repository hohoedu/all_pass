let unpaidStudents = [];

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

    const getCurrentDateTime = () => {
        const now = new Date();
        const yy = now.getFullYear();
        const mm = String(now.getMonth() + 1).padStart(2, "0");
        const dd = String(now.getDate()).padStart(2, "0");
        const hh = String(now.getHours()).padStart(2, "0");
        const min = String(now.getMinutes()).padStart(2, "0");
        return `${yy}-${mm}-${dd} ${hh}:${min}`;
    };

    const closeAllModals = () =>
        document.querySelectorAll(".modal").forEach(m => m.style.display = "none");


    /* -----------------------------
        날짜 초기화 + 변경
    ----------------------------- */

    function initMonth() {
        const now = new Date();
        const yy = now.getFullYear();
        const mm = String(now.getMonth() + 1).padStart(2, "0");

        if (monthInput) monthInput.value = `${yy}-${mm}`;
        if (currentMonth) currentMonth.textContent = `${yy}년 ${parseInt(mm)}월`;
    }

    const handleMonthChange = () => {
        if (!monthInput || !currentMonth) return;
        const [year, month] = monthInput.value.split("-");
        currentMonth.textContent = `${year}년 ${parseInt(month)}월`;
    };

    monthInput?.addEventListener("change", handleMonthChange);
    calendarBtn?.addEventListener("click", () => monthInput.showPicker());
    initMonth();


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

    const openAddPaymentModal = async () => {
        try {
            closeAllModals();

            const res = await fetch("/pay/list/students", {
                method: "POST",
                headers: {"Content-Type": "application/json"}
            });

            if (!res.ok) throw new Error("학생 조회 실패");

            const students = await res.json();
            unpaidStudents = students.response || [];

            renderStudentList(unpaidStudents);

            const paidDateCell = document.querySelector("#paid-student td:nth-child(4)");
            if (paidDateCell) paidDateCell.textContent = getCurrentDateTime();

            modalPayment.style.display = "block";

        } catch (err) {
            alert("납부내역 추가 중 오류가 발생했습니다.");
        }
    };

    btnAddPayment?.addEventListener("click", openAddPaymentModal);


    document.querySelector("#save-pay").addEventListener("click", async () => {

        const selectedRow = document.querySelector("#unpaid-student-list tr.selected");
        if (!selectedRow) {
            alert("학생을 선택하세요.");
            return;
        }

        const dto = {
            studentId: selectedRow.dataset.studentId,
            year: monthInput.value.split("-")[0],
            month: monthInput.value.split("-")[1],
            eduFee: document.querySelector('input[name="eduFee"]').checked,
            bookFee: document.querySelector('input[name="bookFee"]').checked,
            eduCard: Number(document.querySelector('.pay-edu-table .border-green')?.value || 0),
            eduCash: Number(document.querySelector('.pay-edu-table .border-blue')?.value || 0),
            eduTransfer: Number(document.querySelector('.pay-edu-table .border-olive')?.value || 0),
            bookCard: Number(document.querySelectorAll('.pay-edu-table .border-green')[1]?.value || 0),
            bookCash: Number(document.querySelectorAll('.pay-edu-table .border-blue')[1]?.value || 0),
            bookTransfer: Number(document.querySelectorAll('.pay-edu-table .border-olive')[1]?.value || 0)
        };
        try {
            const res = await fetch("/pay/manual", {
                method: "POST",
                headers: {"Content-Type": "application/json"},
                body: JSON.stringify(dto)
            });

            const result = await res.json();

            if (result.success) {

                const destroyRes = await fetch("/pay/destroy/bill", {
                    method: "POST",
                    headers: {"Content-Type": "application/json"},
                    body: JSON.stringify({
                        paymentKey: result.response.paymentKey,
                        billId: result.response.billId,
                        price: result.response.price,
                        studentId: result.response.studentId
                    })
                });

                const destroyResult = await destroyRes.json();

                if (!destroyResult.success) {
                    alert("청구서 파기 실패: " + destroyResult.message);
                    return;
                }

                alert("수기 결제가 완료되었습니다.");

                location.reload();
            } else {
                alert("수기 결제 실패: " + result.message);
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

    const togglePrepayRow = () => {
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
                        ${[2, 3, 4, 5, 6].map(m => `<label><input type="radio" name="prepay-period" value="${m}"> ${m}개월</label>`).join("")}
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
    };

    prepayAddBtn?.addEventListener("click", togglePrepayRow);


});